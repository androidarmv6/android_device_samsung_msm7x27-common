# Copyright (C) 2007 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# BoardConfigCommon.mk
#
# Product-common compile-time definitions.
#

## Define BOARD_HAVE_BLUETOOTH_BLUEZ before device/qcom/msm7x27/BoardConfigCommon.mk
## Bluetooth
BOARD_HAVE_BLUETOOTH_BCM := true
BOARD_HAVE_BLUETOOTH_BLUEZ := true
BOARD_HAVE_SAMSUNG_BLUETOOTH := true
#BOARD_BLUEDROID_VENDOR_CONF := device/samsung/msm7x27-common/bluetooth/vnd_samsung.txt

# Use the Qualcomm common folder
include device/qcom/msm7x27/BoardConfigCommon.mk

## Kernel
BOARD_KERNEL_BASE := 0x13600000
BOARD_KERNEL_PAGESIZE := 4096
ifdef BUILD_WITH_30X_KERNEL
	TARGET_KERNEL_SOURCE := kernel/samsung/msm
else
	TARGET_KERNEL_SOURCE := kernel/samsung/msm7x27
endif
TARGET_PROVIDES_INIT_TARGET_RC := true

## Platform
TARGET_BOARD_PLATFORM_GPU := qcom-adreno200
ifdef BUILD_WITH_30X_KERNEL
	TARGET_SPECIFIC_HEADER_PATH := device/samsung/msm7x27-common/include-30x
else
	TARGET_SPECIFIC_HEADER_PATH := device/samsung/msm7x27-common/include
endif

## Webkit
ENABLE_WEBGL := true
TARGET_WEBKIT_USE_MORE_MEMORY := true

## Camera
USE_CAMERA_STUB := false
BOARD_USE_NASTY_PTHREAD_CREATE_HACK := true

## Qualcomm, display
ifdef BUILD_WITH_30X_KERNEL
	TARGET_NO_HW_VSYNC := false
endif
COMMON_GLOBAL_CFLAGS += -DREFRESH_RATE=60
COMMON_GLOBAL_CFLAGS += -DSAMSUNG_CAMERA_QCOM
COMMON_GLOBAL_CFLAGS += -DBINDER_COMPAT
BOARD_EGL_NEEDS_LEGACY_FB := true
TARGET_GLOBAL_CPPFLAGS += -mfpu=vfp -mfloat-abi=softfp -Os

## GPS
BOARD_USES_QCOM_GPS := true
BOARD_VENDOR_QCOM_GPS_LOC_API_AMSS_VERSION := 50000
BOARD_VENDOR_QCOM_GPS_LOC_API_HARDWARE := msm7x27
BOARD_USES_QCOM_LIBRPC := true

## FM
#BOARD_HAVE_QCOM_FM := true
#BOARD_HAVE_FM_RADIO := true
#COMMON_GLOBAL_CFLAGS += -DHAVE_FM_RADIO -DQCOM_FM_ENABLED
#BOARD_FM_DEVICE := bcm2049

## Wi-Fi
BOARD_WLAN_NO_FWRELOAD := true
COMMON_GLOBAL_CFLAGS += -DWIFI_AP_HAS_OWN_DRIVER
WIFI_AP_FIRMWARE_LOADER := ""
WPA_SUPPLICANT_VERSION := VER_0_8_X

ifeq ($(BOARD_WLAN_DEVICE),ath6kl_compat)
	# This is unnecessary, and breaks WIFI_EXT_MODULE_*
	BOARD_HAVE_SAMSUNG_WIFI := false

	# ATH6KL uses NL80211 driver
	BOARD_WPA_SUPPLICANT_DRIVER := NL80211
	BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_ath6kl_compat

	# ATH6KL uses hostapd built from source
	BOARD_HOSTAPD_DRIVER := NL80211
	BOARD_HOSTAPD_PRIVATE_LIB := lib_driver_cmd_ath6kl_compat

	# Common module dependency
	WIFI_EXT_MODULE_NAME := cfg80211
	WIFI_EXT_MODULE_PATH := /system/lib/modules/cfg80211.ko

	# AP mode
	WIFI_AP_DRIVER_MODULE_ARG := "suspend_mode=3 wow_mode=2 ath6kl_p2p=1 recovery_enable=1 samsung_firmware=0"
	WIFI_AP_DRIVER_MODULE_NAME := ath6kl
	WIFI_AP_DRIVER_MODULE_PATH := /system/lib/modules/ath6kl.ko

	# Station/client mode
	WIFI_DRIVER_MODULE_ARG := "suspend_mode=3 wow_mode=2 ath6kl_p2p=1 recovery_enable=1 samsung_firmware=1"
	WIFI_DRIVER_MODULE_NAME := ath6kl
	WIFI_DRIVER_MODULE_PATH := /system/lib/modules/ath6kl.ko

	# Build the ath6kl-compat modules
KERNEL_EXTERNAL_MODULES:
	# wipe & prepare ath6kl-compat working directory
	rm -rf $(OUT)/ath6kl-compat
	cp -a hardware/atheros/ath6kl-compat $(OUT)/
	# run build
	$(MAKE) -C $(OUT)/ath6kl-compat KERNEL_DIR=$(KERNEL_OUT) KLIB=$(KERNEL_OUT) KLIB_BUILD=$(KERNEL_OUT) ARCH=$(TARGET_ARCH) $(ARM_CROSS_COMPILE)
	# copy & strip modules (to economize space)
	$(TARGET_OBJCOPY) --strip-unneeded $(OUT)/ath6kl-compat/compat/compat.ko $(KERNEL_MODULES_OUT)/compat.ko
	$(TARGET_OBJCOPY) --strip-unneeded $(OUT)/ath6kl-compat/drivers/net/wireless/ath/ath6kl/ath6kl.ko $(KERNEL_MODULES_OUT)/ath6kl.ko
	$(TARGET_OBJCOPY) --strip-unneeded $(OUT)/ath6kl-compat/net/wireless/cfg80211.ko $(KERNEL_MODULES_OUT)/cfg80211.ko
TARGET_KERNEL_MODULES := KERNEL_EXTERNAL_MODULES
else
	# Enhance Samsung AR6000 compatibility
	BOARD_HAVE_SAMSUNG_WIFI := true

	# AR6000 SDK 3.x uses WEXT driver
	BOARD_WLAN_DEVICE := ath6kl
	BOARD_WPA_SUPPLICANT_DRIVER := WEXT
	BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_wext

	# AP mode
	WIFI_AP_DRIVER_MODULE_ARG := "ifname=athap0 fwmode=2"
	WIFI_AP_DRIVER_MODULE_PATH := /system/wifi/ar6000.ko
	WIFI_AP_DRIVER_MODULE_NAME := ar6000

	# Station/client mode
	WIFI_DRIVER_MODULE_ARG := "ifname=wlan0 fwmode=1"
	WIFI_DRIVER_MODULE_PATH := /system/wifi/ar6000.ko
	WIFI_DRIVER_MODULE_NAME := ar6000
endif

## Wi-Fi Hotspot
BOARD_HAVE_LEGACY_HOSTAPD := true
BOARD_HOSTAPD_NO_ENTROPY := true

## RIL
TARGET_PROVIDES_LIBRIL := true
BOARD_USES_LEGACY_RIL := true
BOARD_FORCE_RILD_AS_ROOT := true
BOARD_MOBILEDATA_INTERFACE_NAME := "pdp0"
BOARD_RIL_CLASS := ../../../device/samsung/msm7x27-common/ril/

## UMS
TARGET_USE_CUSTOM_LUN_FILE_PATH := /sys/devices/platform/msm_hsusb/gadget/lun0/file
BOARD_UMS_LUNFILE := "/sys/devices/platform/msm_hsusb/gadget/lun0/file"

## Legacy touchscreen support
BOARD_USE_LEGACY_TOUCHSCREEN := true

## Device specific libs
TARGET_PROVIDES_LIBAUDIO := true
TARGET_PROVIDES_LIBLIGHT := true

## Audio: combo device supported
BOARD_COMBO_DEVICE_SUPPORTED := true

## Samsung has weird framebuffer
TARGET_NO_INITLOGO := true

## Fix colors in panorama mode
BOARD_CPU_COLOR_CONVERT := true

## Recovery
BOARD_HAS_DOWNLOAD_MODE := true
TARGET_USERIMAGES_USE_EXT4 := true
BOARD_BOOTIMAGE_PARTITION_SIZE := 8388608
BOARD_RECOVERYIMAGE_PARTITION_SIZE := 8388608
BOARD_SYSTEMIMAGE_PARTITION_SIZE := 219938816
BOARD_USERDATAIMAGE_PARTITION_SIZE := 190054400
BOARD_FLASH_BLOCK_SIZE := 131072
BOARD_KERNEL_CMDLINE :=
BOARD_BML_BOOT := "/dev/block/bml8"
BOARD_BML_RECOVERY := "/dev/block/bml9"
BOARD_RECOVERY_HANDLES_MOUNT := true

## OTA script extras file (build/tools/releasetools)
TARGET_OTA_EXTRAS_FILE := device/samsung/msm7x27-common/releasetools-extras.txt
