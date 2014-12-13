LOCAL_PATH:= $(call my-dir)

ifeq ($(BOARD_SWAP_SYSTEMDATA),true)
	BOARD_STL_DATA   := "/dev/block/stl12"
	BOARD_STL_SYSTEM := "/dev/block/stl13"
else
	BOARD_STL_DATA   := "/dev/block/stl13"
	BOARD_STL_SYSTEM := "/dev/block/stl12"
endif

ifeq ($(RECOVERY_VARIANT),twrp)
#######################################
# twrp.fstab

include $(CLEAR_VARS)
LOCAL_MODULE		:= twrp.fstab
LOCAL_MODULE_TAGS	:= optional
LOCAL_MODULE_CLASS	:= ETC
LOCAL_SRC_FILES		:= twrp.fstab
LOCAL_MODULE_PATH	:= $(TARGET_RECOVERY_ROOT_OUT)/etc
include $(BUILD_PREBUILT)

$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/$(LOCAL_SRC_FILES)
	@echo "Adjusting partiton configuration for twrp.fstab: $< -> $@"
	@mkdir -p $(dir $@)
	$(hide) sed -e s#'BOARD_BML_BOOT'#$(BOARD_BML_BOOT)#g -e s#'BOARD_BML_RECOVERY'#$(BOARD_BML_RECOVERY)#g -e s#'BOARD_STL_DATA'#$(BOARD_STL_DATA)#g -e s#'BOARD_STL_SYSTEM'#$(BOARD_STL_SYSTEM)#g $< >$@
endif

#######################################
# fstab.gt-xxxxx

include $(CLEAR_VARS)
LOCAL_MODULE		:= fstab.$(SAMSUNG_BOOTLOADER)
LOCAL_MODULE_TAGS	:= optional
LOCAL_MODULE_CLASS	:= ETC
ifeq ($(BOARD_SWAP_SYSTEMDATA),true)
	LOCAL_SRC_FILES		:= fstab.msm7x27_swapped
else
	LOCAL_SRC_FILES		:= fstab.msm7x27
endif
LOCAL_MODULE_PATH	:= $(TARGET_ROOT_OUT)
include $(BUILD_PREBUILT)

$(LOCAL_BUILT_MODULE): $(LOCAL_PATH)/$(LOCAL_SRC_FILES)
	@echo "Adjusting mmc & zram configuration for fstab.$(SAMSUNG_BOOTLOADER): $< -> $@"
	@mkdir -p $(dir $@)
	$(hide) sed -e s#'/dev/block/mmcblk0\t\t\t'#'/devices/platform/msm_sdcc.1/mmc_host/mmc0*'#g -e 's/50331648/$(BOARD_ZRAM_SIZE)/g' $< >$@

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
	@echo "Adjust init rc script for $(SAMSUNG_BOOTLOADER): $< -> $@"
	@mkdir -p $(dir $@)
	$(hide) sed -e 's/fstab.msm7x27/fstab.$(SAMSUNG_BOOTLOADER)/g' $< >$@

#######################################
# init.gt-xxxxx.bluetooth.rc

include $(CLEAR_VARS)
LOCAL_MODULE		:= init.$(SAMSUNG_BOOTLOADER).bluetooth.rc
LOCAL_MODULE_TAGS	:= optional
LOCAL_MODULE_CLASS	:= ETC
LOCAL_SRC_FILES		:= init.msm7x27.bluedroid.rc
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
