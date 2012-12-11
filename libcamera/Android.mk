LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS 	    += -DANDROID_JB=1 
##Uncomment previous line in JB build

LOCAL_CFLAGS        += -O3
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)/hw
LOCAL_MODULE := camera.$(TARGET_DEVICE)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := cameraHAL.cpp
LOCAL_C_INCLUDES := $(TOP)/frameworks/base/include
LOCAL_C_INCLUDES       += hardware/qcom/display/libgralloc

LOCAL_SHARED_LIBRARIES := liblog libutils libcutils
LOCAL_SHARED_LIBRARIES += libui libhardware libcamera_client

LOCAL_LDFLAGS	       += -L$(LOCAL_PATH) -lcamera

include $(BUILD_SHARED_LIBRARY)
