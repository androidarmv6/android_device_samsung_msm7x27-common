LOCAL_PATH := $(call my-dir)

include $(call all-subdir-makefiles)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS    := optional
LOCAL_MODULE_PATH    := $(TARGET_OUT_SHARED_LIBRARIES)/hw
LOCAL_MODULE         := camera.$(TARGET_DEVICE)
LOCAL_SRC_FILES      := cameraHAL.cpp

LOCAL_SHARED_LIBRARIES := liblog libdl libutils libcamera_client libbinder libcutils libhardware libui

LOCAL_LDFLAGS		+= -L$(LOCAL_PATH) -lcamera
LOCAL_C_INCLUDES       := frameworks/av/include frameworks/base/include frameworks/native/include
LOCAL_C_INCLUDES       += hardware/libhardware/include/ hardware
LOCAL_C_INCLUDES       += hardware/qcom/display/libgralloc

include $(BUILD_SHARED_LIBRARY)
