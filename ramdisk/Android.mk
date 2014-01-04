LOCAL_PATH:= $(call my-dir)

#######################################
# fstab.gt-xxxxx

include $(CLEAR_VARS)
LOCAL_MODULE		:= fstab.$(SAMSUNG_BOOTLOADER)
LOCAL_MODULE_TAGS	:= optional
LOCAL_MODULE_CLASS	:= ETC
LOCAL_SRC_FILES		:= fstab.msm7x27
LOCAL_MODULE_PATH	:= $(TARGET_ROOT_OUT)
include $(BUILD_PREBUILT)

#######################################
# init.gt-xxxxx.rc

include $(CLEAR_VARS)
LOCAL_MODULE		:= init.$(SAMSUNG_BOOTLOADER).rc
LOCAL_MODULE_TAGS	:= optional
LOCAL_MODULE_CLASS	:= ETC
LOCAL_SRC_FILES		:= init.msm7x27.rc
LOCAL_MODULE_PATH	:= $(TARGET_ROOT_OUT)

include $(BUILD_PREBUILT)

$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/$(LOCAL_SRC_FILES)
	@echo "Adjust init service for $(SAMSUNG_BOOTLOADER): $< -> $@"
	@mkdir -p $(dir $@)
ifneq (,$(filter galaxy5,$(CM_BUILD)))
	$(hide) sed -e 's/fstab.msm7x27/fstab.$(SAMSUNG_BOOTLOADER)/g' \
	-e 's/memsicd/g5sensord/g' $< >$@
else
	$(hide) sed -e 's/fstab.msm7x27/fstab.$(SAMSUNG_BOOTLOADER)/g' $< >$@
endif

#######################################
# init.gt-xxxxx.bluetooth.rc

include $(CLEAR_VARS)
LOCAL_MODULE		:= init.$(SAMSUNG_BOOTLOADER).bluetooth.rc
LOCAL_MODULE_TAGS	:= optional
LOCAL_MODULE_CLASS	:= ETC
ifeq ($(BOARD_HAVE_BLUETOOTH_BLUEZ),true)
	LOCAL_SRC_FILES		:= init.msm7x27.bluez.rc
else
	LOCAL_SRC_FILES		:= init.msm7x27.bluedroid.rc
endif
LOCAL_MODULE_PATH	:= $(TARGET_ROOT_OUT)

include $(BUILD_PREBUILT)

ifneq (,$(filter callisto galaxy5,$(CM_BUILD)))
$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/$(LOCAL_SRC_FILES)
	@echo "Adjust Bluetooth firmware for $(SAMSUNG_BOOTLOADER): $< -> $@"
	@mkdir -p $(dir $@)
	$(hide) sed -e 's/BCM2049C0_003.001.031.0088.0094.hcd/BCM2049B0_BCM20780B0_002.001.022.0170.0174.hcd/g' \
	$< >$@
endif

#######################################
# init.gt-xxxxx.parts.rc

include $(CLEAR_VARS)
LOCAL_MODULE		:= init.$(SAMSUNG_BOOTLOADER).parts.rc
LOCAL_MODULE_TAGS	:= optional
LOCAL_MODULE_CLASS	:= ETC
LOCAL_SRC_FILES		:= init.msm7x27.parts.rc
LOCAL_MODULE_PATH	:= $(TARGET_ROOT_OUT)
include $(BUILD_PREBUILT)

#######################################
# init.gt-xxxxx.usb.rc

include $(CLEAR_VARS)
LOCAL_MODULE		:= init.$(SAMSUNG_BOOTLOADER).usb.rc
LOCAL_MODULE_TAGS	:= optional
LOCAL_MODULE_CLASS	:= ETC
LOCAL_SRC_FILES		:= init.msm7x27.usb.rc
LOCAL_MODULE_PATH	:= $(TARGET_ROOT_OUT)
include $(BUILD_PREBUILT)

#######################################
# init.recovery.gt-xxxxx.rc

include $(CLEAR_VARS)
LOCAL_MODULE		:= init.recovery.$(SAMSUNG_BOOTLOADER).rc
LOCAL_MODULE_TAGS	:= optional
LOCAL_MODULE_CLASS	:= ETC
LOCAL_SRC_FILES		:= init.recovery.msm7x27.rc
LOCAL_MODULE_PATH	:= $(TARGET_ROOT_OUT)
include $(BUILD_PREBUILT)

#######################################
# ueventd.gt-xxxxx.rc

include $(CLEAR_VARS)
LOCAL_MODULE		:= ueventd.$(SAMSUNG_BOOTLOADER).rc
LOCAL_MODULE_TAGS	:= optional
LOCAL_MODULE_CLASS	:= ETC
LOCAL_SRC_FILES		:= ueventd.msm7x27.rc
LOCAL_MODULE_PATH	:= $(TARGET_ROOT_OUT)
include $(BUILD_PREBUILT)

#######################################
