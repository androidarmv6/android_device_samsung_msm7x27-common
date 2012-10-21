/*
 * Copyright (C) 2012 The Android Open Source Project
 * Copyright (C) 2012 Zhibin Wu, Simon Davie, Nico Kaiser
 * Copyright (C) 2012 QiSS ME Project Team
 * Copyright (C) 2012 Twisted, Sean Neeley
 * Copyright (C) 2012 GalaxyICS
 * Copyright (C) 2012 Pavel Kirpichyov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "CameraHAL"

#define MAX_CAMERAS_SUPPORTED 1
#define GRALLOC_USAGE_PMEM_PRIVATE_ADSP GRALLOC_USAGE_PRIVATE_0

#include <CameraHardwareInterface.h>
#include <hardware/hardware.h>
#include <hardware/camera.h>
#include <binder/IMemory.h>
#include <fcntl.h>
#include <linux/ioctl.h>
#include <linux/msm_mdp.h>
#include <ui/Rect.h>
#include <ui/GraphicBufferMapper.h>
#include <dlfcn.h>
#include <gralloc_priv.h>

#define NO_ERROR 0

using android::sp;
//using android::Overlay;
using android::String8;
using android::IMemory;
using android::IMemoryHeap;
using android::CameraParameters;

using android::CameraInfo;
using android::HAL_getCameraInfo;
using android::HAL_getNumberOfCameras;
using android::HAL_openCameraHardware;
using android::CameraHardwareInterface;

static int camera_device_open(const hw_module_t* module, const char* name, hw_device_t** device);
static int camera_device_close(hw_device_t* device);
static int camera_get_number_of_cameras(void);
static int camera_get_camera_info(int camera_id, struct camera_info *info);

static struct hw_module_methods_t camera_module_methods = {
	open: camera_device_open
};

camera_module_t HAL_MODULE_INFO_SYM = {
	common: {
		tag: HARDWARE_MODULE_TAG,
		version_major: 1,
		version_minor: 0,
		id: CAMERA_HARDWARE_MODULE_ID,
		name: "GalaxyICS Camera HAL",
		author: "Marcin Chojnacki & Pavel Kirpichyov",
		methods: &camera_module_methods,
		dso: NULL, /* remove compilation warnings */
		reserved: {0}, /* remove compilation warnings */
	},
	get_number_of_cameras: camera_get_number_of_cameras,
	get_camera_info: camera_get_camera_info,
};

static struct {
    int type;
    const char *text;
} msg_map[] = {
    {0x0001, "CAMERA_MSG_ERROR"},
    {0x0002, "CAMERA_MSG_SHUTTER"},
    {0x0004, "CAMERA_MSG_FOCUS"},
    {0x0008, "CAMERA_MSG_ZOOM"},
    {0x0010, "CAMERA_MSG_PREVIEW_FRAME"},
    {0x0020, "CAMERA_MSG_VIDEO_FRAME"},
    {0x0040, "CAMERA_MSG_POSTVIEW_FRAME"},
    {0x0080, "CAMERA_MSG_RAW_IMAGE"},
    {0x0100, "CAMERA_MSG_COMPRESSED_IMAGE"},
    {0x0200, "CAMERA_MSG_RAW_IMAGE_NOTIFY"},
    {0x0400, "CAMERA_MSG_PREVIEW_METADATA"},
    {0x0000, "CAMERA_MSG_ALL_MSGS"}, //0xFFFF
    {0x0000, "NULL"},
};

android::String8          g_str;
android::CameraParameters camSettings;
preview_stream_ops_t      *mWindow = NULL;
android::sp<android::CameraHardwareInterface> qCamera;

camera_notify_callback         origNotify_cb    = NULL;
camera_data_callback           origData_cb      = NULL;
camera_data_timestamp_callback origDataTS_cb    = NULL;
camera_request_memory          origCamReqMemory = NULL;


static void dump_msg(const char *tag, int msg_type)
{
    int i;
    for (i = 0; msg_map[i].type; i++) {
        if (msg_type & msg_map[i].type) {
            ALOGI("%s: %s", tag, msg_map[i].text);
        }
    }
}

void CameraHal_Decode_Sw(unsigned int* rgb, char* yuv420sp, int width, int height)
{
   int frameSize = width * height;
   int yp = 0;
   for (int j = 0, yp = 0; j < height; j++) {
      int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
      for (int i = 0; i < width; i++, yp++) {
         int y = (0xff & ((int) yuv420sp[yp])) - 16;
         if (y < 0) y = 0;
         if ((i & 1) == 0) {
            v = (0xff & yuv420sp[uvp++]) - 128;
            u = (0xff & yuv420sp[uvp++]) - 128;
         }

         int y1192 = 1192 * y;
         int r = (y1192 + 1634 * v);
         int g = (y1192 - 833 * v - 400 * u);
         int b = (y1192 + 2066 * u);

		 if (r < 0) r = 0; else if (r > 262143) r = 262143;
         if (g < 0) g = 0; else if (g > 262143) g = 262143;
         if (b < 0) b = 0; else if (b > 262143) b = 262143;

         rgb[yp] = 0xff000000 | ((b << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((r >> 10) & 0xff);
      }
   }
}

camera_memory_t * CameraHAL_GenClientData(const android::sp<android::IMemory> &dataPtr, void *user)
{
	if(!origCamReqMemory)
	{
		return NULL;
	}

	ssize_t          offset;
	size_t           size;
	camera_memory_t *clientData;
	android::sp<android::IMemoryHeap> mHeap	= dataPtr->getMemory(&offset, &size);
	void *data = (void *)((char *)(mHeap->base()) + offset);

	ALOGV("CameraHAL_GenClientData: offset:%#x size:%#x base:%p\n", (unsigned)offset, size, mHeap != NULL ? mHeap->base() : 0);

	clientData = origCamReqMemory(-1, size, 1, user);

	memcpy(clientData->data, data, size);
	return clientData;
}


void CameraHAL_HandlePreviewData(const sp<IMemory>& dataPtr, preview_stream_ops_t *mWindow, int32_t previewWidth, int32_t previewHeight)
{
	if (mWindow == NULL || origCamReqMemory == NULL) return;

	ssize_t  offset;
	size_t   size;
	int32_t  previewFormat = MDP_Y_CBCR_H2V2;
	int32_t  destFormat    = MDP_RGBA_8888;

	android::status_t retVal;
	sp<IMemoryHeap> mHeap = dataPtr->getMemory(&offset, &size);

	ALOGV("CameraHAL_HandlePreviewData: previewWidth:%d previewHeight:%d offset:%#x size:%#x base:%p\n", previewWidth, previewHeight, (unsigned)offset, size, mHeap != NULL ? mHeap->base() : 0);

	mWindow->set_usage(mWindow, GRALLOC_USAGE_PMEM_PRIVATE_ADSP | GRALLOC_USAGE_SW_READ_OFTEN);

	retVal = mWindow->set_buffers_geometry(mWindow, previewWidth, previewHeight, HAL_PIXEL_FORMAT_RGBX_8888);
	if (retVal != NO_ERROR) return;

	int32_t          stride;
	buffer_handle_t *bufHandle = NULL;

	ALOGV("CameraHAL_HandlePreviewData: dequeueing buffer\n");
	retVal = mWindow->dequeue_buffer(mWindow, &bufHandle, &stride);
	if (retVal != NO_ERROR) {
		ALOGV("CameraHAL_HandlePreviewData: ERROR dequeueing the buffer\n");
		return;
	}

	retVal = mWindow->lock_buffer(mWindow, bufHandle);
	if (retVal != NO_ERROR) {
		ALOGV("CameraHAL_HandlePreviewData: ERROR locking the buffer\n");
		mWindow->cancel_buffer(mWindow, bufHandle);
		return;
	}
	private_handle_t const *privHandle = reinterpret_cast<private_handle_t const *>(*bufHandle);
	void *bits;
	android::Rect bounds;
	android::GraphicBufferMapper &mapper = android::GraphicBufferMapper::get();

	bounds.left   = 0;
	bounds.top    = 0;
	bounds.right  = previewWidth;
	bounds.bottom = previewHeight;

	mapper.lock(*bufHandle, GRALLOC_USAGE_SW_READ_OFTEN, bounds, &bits);
	CameraHal_Decode_Sw((unsigned int *)bits, (char *)mHeap->base() + offset, previewWidth, previewHeight);
	// unlock buffer before sending to display
	mapper.unlock(*bufHandle);

	mWindow->enqueue_buffer(mWindow, bufHandle);
	ALOGV("CameraHAL_HandlePreviewData: enqueued buffer\n");
}

static void wrap_notify_callback(int32_t msg_type, int32_t ext1, int32_t ext2, void* user)
{
	ALOGV("CameraHAL_NotifyCb: msg_type:%d ext1:%d ext2:%d user:%p\n", msg_type, ext1, ext2, user);
	if (origNotify_cb != NULL) {
		origNotify_cb(msg_type, ext1, ext2, user);
	}

	ALOGD("%s---", __FUNCTION__);
}

static void wrap_data_callback(int32_t msg_type, const sp<IMemory>& dataPtr, void* user)
{
	ALOGV("wrap_data_callback: msg_type: %d user: %p\n", msg_type, user);

	if (msg_type == CAMERA_MSG_PREVIEW_FRAME) {

		int32_t previewWidth, previewHeight;
		android::CameraParameters hwParameters = qCamera->getParameters();
		hwParameters.getPreviewSize(&previewWidth, &previewHeight);
		CameraHAL_HandlePreviewData(dataPtr, mWindow, previewWidth, previewHeight);
	} 

	camera_memory_t *clientData = CameraHAL_GenClientData(dataPtr, user);
	ALOGV("CameraHAL_DataCb: Posting data to client\n");

	if(origData_cb)
		origData_cb(msg_type, clientData, 0, NULL, user);

	if (clientData != NULL) 
		clientData->release(clientData);
	
	ALOGV("wrap_data_callbaak--\n");
}

static void wrap_data_callback_timestamp(nsecs_t timestamp, int32_t msg_type, const sp<IMemory>& dataPtr, void* user)
{
	camera_memory_t *clientData = CameraHAL_GenClientData(dataPtr, user);
	if (origDataTS_cb != NULL) {
		origDataTS_cb(timestamp, msg_type, clientData, 0, user);
	}

	qCamera->releaseRecordingFrame(dataPtr);

	if (clientData != NULL) {
		ALOGV("CameraHAL_DataTSCb: Posting data to client timestamp:%lld\n", systemTime());
		clientData->release(clientData);
	} else {
		ALOGD("CameraHAL_DataTSCb: ERROR allocating memory from client\n");
	}
}

/*******************************************************************
 * implementation of camera_device_ops functions
 *******************************************************************/

void CameraHAL_FixupParams(android::CameraParameters &camParams)
{
    const char *preferred_size = "640x480";

    camParams.set(android::CameraParameters::KEY_VIDEO_FRAME_FORMAT, android::CameraParameters::PIXEL_FORMAT_YUV420SP);

    camParams.set(CameraParameters::KEY_PREFERRED_PREVIEW_SIZE_FOR_VIDEO, preferred_size);

    if (!camParams.get(android::CameraParameters::KEY_MAX_NUM_FOCUS_AREAS)) {
        camParams.set(CameraParameters::KEY_MAX_NUM_FOCUS_AREAS, 1);
    }

    // zoom
   // camParams.set(CameraParameters::KEY_MAX_ZOOM, "12");
   // camParams.set(CameraParameters::KEY_ZOOM_RATIOS, "100,125,150,175,200,225,250,275,300,325,350,375,400");
   // camParams.set(CameraParameters::KEY_ZOOM_SUPPORTED, CameraParameters::TRUE);

    camParams.set(CameraParameters::KEY_MAX_EXPOSURE_COMPENSATION, 4);
    camParams.set(CameraParameters::KEY_MIN_EXPOSURE_COMPENSATION, -4);
    camParams.set(CameraParameters::KEY_EXPOSURE_COMPENSATION_STEP, 1);
}

int camera_set_preview_window(struct camera_device * device, struct preview_stream_ops *window) {
	ALOGV("qcamera_set_preview_window : Window :%p\n", window);
	if (device == NULL) {
		ALOGE("qcamera_set_preview_window : Invalid device.\n");
		return -EINVAL;
	} else {
		ALOGV("qcamera_set_preview_window : window :%p\n", window);
		mWindow = window;
		return 0;
	}
}

void camera_set_callbacks(struct camera_device * device,
                          camera_notify_callback notify_cb,
                          camera_data_callback data_cb,
                          camera_data_timestamp_callback data_cb_timestamp,
                          camera_request_memory get_memory,
                          void *user)
{
	origNotify_cb    = notify_cb;
	origData_cb      = data_cb;
	origDataTS_cb    = data_cb_timestamp;
	origCamReqMemory = get_memory;

    qCamera->setCallbacks(wrap_notify_callback, wrap_data_callback, wrap_data_callback_timestamp, user);

    ALOGI("%s---, device: %p", __FUNCTION__, device);

}

void camera_enable_msg_type(struct camera_device * device, int32_t msg_type)
{
	ALOGI("%s+++: type %i", __FUNCTION__, msg_type);

	if (msg_type == 0xfff) {
      		msg_type = 0x1ff;
   	} else {
      		msg_type &= ~(CAMERA_MSG_PREVIEW_METADATA | CAMERA_MSG_RAW_IMAGE_NOTIFY);
   	}
	qCamera->enableMsgType(msg_type);
	ALOGI("%s---", __FUNCTION__);

}

void camera_disable_msg_type(struct camera_device * device, int32_t msg_type)
{
    ALOGI("%s+++: type %i", __FUNCTION__, msg_type);
    if (msg_type == 0xfff) {
       msg_type = 0x1ff;
    }
    qCamera->disableMsgType(msg_type);
    ALOGI("%s---", __FUNCTION__);

}

int camera_msg_type_enabled(struct camera_device * device, int32_t msg_type)
{
    ALOGI("%s+++: type %i", __FUNCTION__, msg_type);
    return qCamera->msgTypeEnabled(msg_type);
}

int camera_start_preview(struct camera_device * device)
{
	ALOGI("%s+++", __FUNCTION__);

	if (!qCamera->msgTypeEnabled(CAMERA_MSG_PREVIEW_FRAME)) {
		qCamera->enableMsgType(CAMERA_MSG_PREVIEW_FRAME);
	}

	return qCamera->startPreview();
}

void camera_stop_preview(struct camera_device * device)
{
    ALOGI("%s+++", __FUNCTION__);
   if (qCamera->msgTypeEnabled(CAMERA_MSG_PREVIEW_FRAME)) {
       qCamera->disableMsgType(CAMERA_MSG_PREVIEW_FRAME);
   }
    qCamera->stopPreview();
}

int camera_preview_enabled(struct camera_device * device)
{
    ALOGI("%s+++", __FUNCTION__);
    return qCamera->previewEnabled() ? 1 : 0;
}

int camera_store_meta_data_in_buffers(struct camera_device * device, int enable)
{
    return NO_ERROR;
}

int camera_start_recording(struct camera_device * device)
{
    ALOGI("%s+++", __FUNCTION__);
	qCamera->enableMsgType(CAMERA_MSG_VIDEO_FRAME);
        qCamera->startRecording();

        return NO_ERROR;
}

void camera_stop_recording(struct camera_device * device)
{
    ALOGI("%s+++: device", __FUNCTION__);

	qCamera->disableMsgType(CAMERA_MSG_VIDEO_FRAME);
    qCamera->stopRecording();

    //qCamera->startPreview();
    ALOGI("%s---", __FUNCTION__);
}

int camera_recording_enabled(struct camera_device * device)
{
    ALOGI("%s+++", __FUNCTION__);
    return qCamera->recordingEnabled() ? 1 : 0;
}

void camera_release_recording_frame(struct camera_device * device, const void *opaque)
{
    ALOGI("%s---", __FUNCTION__);
}

int camera_auto_focus(struct camera_device * device)
{
    ALOGI("%s+++", __FUNCTION__);
    qCamera->autoFocus();

    return NO_ERROR;
}

int camera_cancel_auto_focus(struct camera_device * device)
{
    ALOGI("%s+++", __FUNCTION__);
    qCamera->cancelAutoFocus();

    return NO_ERROR;
}

int camera_take_picture(struct camera_device * device)
{
    ALOGI("%s+++", __FUNCTION__);

    qCamera->enableMsgType(CAMERA_MSG_SHUTTER | CAMERA_MSG_POSTVIEW_FRAME | CAMERA_MSG_RAW_IMAGE | CAMERA_MSG_COMPRESSED_IMAGE);
    qCamera->takePicture();

    return NO_ERROR;
}

int camera_cancel_picture(struct camera_device * device)
{
    ALOGI("%s+++, device: %p", __FUNCTION__, device);
    qCamera->cancelPicture();

    return NO_ERROR;
}

int camera_set_parameters(struct camera_device * device, const char *params)
{
   ALOGV("qcamera_set_parameters: %s\n", params);
   g_str = android::String8(params);
   camSettings.unflatten(g_str);
   qCamera->setParameters(camSettings);
   return NO_ERROR;
}

char* camera_get_parameters(struct camera_device * device)
{
  	camSettings = qCamera->getParameters();
   	ALOGV("qcamera_get_parameters: after calling qCamera->getParameters()\n");
   	CameraHAL_FixupParams(camSettings);
   	g_str = camSettings.flatten();
   	char* params = (char*) malloc(sizeof(char) * (g_str.length()+1));
	strcpy(params, g_str.string());
 	return params;
}

static void camera_put_parameters(struct camera_device *device, char *parms)
{
    ALOGI("%s+++", __FUNCTION__);
    free(parms);
    ALOGI("%s---", __FUNCTION__);
}

int camera_send_command(struct camera_device * device, int32_t cmd, int32_t arg1, int32_t arg2)
{
    ALOGI("%s: cmd %i", __FUNCTION__, cmd);
    return qCamera->sendCommand(cmd, arg1, arg2);
}

void camera_release(struct camera_device * device)
{
    ALOGI("%s+++, device: %p", __FUNCTION__, device);
    qCamera->release();
    ALOGI("%s---", __FUNCTION__);
}

int camera_dump(struct camera_device * device, int fd)
{
    ALOGI("%s", __FUNCTION__);
    android::Vector<android::String16> args;
    return qCamera->dump(fd, args);
}

extern "C" void heaptracker_free_leaked_memory(void);

int camera_device_close(hw_device_t* device)
{
	int rc = -EINVAL;
	ALOGD("camera_device_close\n");
	camera_device_t *cameraDev = (camera_device_t *)device;
	if (cameraDev) {
		qCamera = NULL;

		if (cameraDev->ops) {
			free(cameraDev->ops);
		}
		free(cameraDev);
		rc = NO_ERROR;
	}
	return rc;
}

/*******************************************************************
 * implementation of camera_module functions
 *******************************************************************/

/* open device handle to one of the cameras
 *
 * assume camera service will keep singleton of each camera
 * so this function will always only be called once per camera instance
 */
void sighandle(int s) {

}

int camera_device_open(const hw_module_t* module, const char* name, hw_device_t** device)
{
    int cameraid;
    int num_cameras				= 0;
    camera_device_t* 		camera_device	= NULL;
    camera_device_ops_t* camera_ops		= NULL;
    signal(SIGFPE,(*sighandle));
    int rv					= 0;

    ALOGE("camera_device open v0.8 +++");

    if (name != NULL) {
        cameraid = atoi(name);

        num_cameras = HAL_getNumberOfCameras();

        if(cameraid > num_cameras)
        {
            ALOGE("camera service provided cameraid out of bounds, "
                 "cameraid = %d, num supported = %d",
                 cameraid, num_cameras);
            rv = -EINVAL;
            goto fail;
        }

        camera_device = (camera_device_t*)malloc(sizeof(*camera_device));
        if(!camera_device)
        {
            ALOGE("camera_device allocation fail");
            rv = -ENOMEM;
            goto fail;
        }

        camera_ops = (camera_device_ops_t*)malloc(sizeof(*camera_ops));
        if(!camera_ops)
        {
            ALOGE("camera_ops allocation fail");
            rv = -ENOMEM;
            goto fail;
        }

        memset(camera_device, 0, sizeof(*camera_device));
        memset(camera_ops, 0, sizeof(*camera_ops));

        camera_device->common.tag			= HARDWARE_DEVICE_TAG;
        camera_device->common.version			= 0;
        camera_device->common.module			= (hw_module_t *)(module);
        camera_device->common.close			= camera_device_close;
        camera_device->ops				= camera_ops;

        camera_ops->set_preview_window			= camera_set_preview_window;
        camera_ops->set_callbacks			= camera_set_callbacks;
        camera_ops->enable_msg_type			= camera_enable_msg_type;
        camera_ops->disable_msg_type			= camera_disable_msg_type;
        camera_ops->msg_type_enabled			= camera_msg_type_enabled;
        camera_ops->start_preview			= camera_start_preview;
        camera_ops->stop_preview			= camera_stop_preview;
        camera_ops->preview_enabled			= camera_preview_enabled;
        camera_ops->store_meta_data_in_buffers		= camera_store_meta_data_in_buffers;
        camera_ops->start_recording			= camera_start_recording;
        camera_ops->stop_recording			= camera_stop_recording;
        camera_ops->recording_enabled			= camera_recording_enabled;
        camera_ops->release_recording_frame		= camera_release_recording_frame;
        camera_ops->auto_focus				= camera_auto_focus;
        camera_ops->cancel_auto_focus			= camera_cancel_auto_focus;
        camera_ops->take_picture			= camera_take_picture;
        camera_ops->cancel_picture			= camera_cancel_picture;
        camera_ops->set_parameters			= camera_set_parameters;
        camera_ops->get_parameters			= camera_get_parameters;
        camera_ops->put_parameters			= camera_put_parameters;
        camera_ops->send_command			= camera_send_command;
        camera_ops->release				= camera_release;
        camera_ops->dump				= camera_dump;

        *device = &camera_device->common;

	qCamera = HAL_openCameraHardware(cameraid);
        if(qCamera == NULL)
        {
            ALOGE("Couldn't create instance of CameraHal class");
            rv = -ENOMEM;
            goto fail;
        }
    }
    ALOGI("%s---ok rv %d", __FUNCTION__,rv);

    return rv;

fail:
    if(camera_device) {
        free(camera_device);
        camera_device = NULL;
    }
    if(camera_ops) {
        free(camera_ops);
        camera_ops = NULL;
    }
    *device = NULL;
    ALOGI("%s--- fail rv %d", __FUNCTION__,rv);

    return rv;
}

int camera_get_number_of_cameras(void)
{
    int num_cameras = HAL_getNumberOfCameras();

    ALOGI("%s: number:%i", __FUNCTION__, num_cameras);

    return num_cameras;
}

int camera_get_camera_info(int camera_id, struct camera_info *info)
{
    int rv = 0;

    CameraInfo cameraInfo;

    android::HAL_getCameraInfo(camera_id, &cameraInfo);

    info->facing = cameraInfo.facing;
    //info->orientation = cameraInfo.orientation;
    if(info->facing == 1) {
        info->orientation = 270;
    } else {
        info->orientation = 90;
    }

    ALOGI("%s: id:%i faceing:%i orientation: %i", __FUNCTION__,camera_id, info->facing, info->orientation);
    return rv;
}
