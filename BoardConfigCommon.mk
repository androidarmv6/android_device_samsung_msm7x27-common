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

LOCAL_PATH:= $(call my-dir)

include vendor/samsung/msm7x27-common/BoardConfigVendor.mk

## Kernel
TARGET_NO_BOOTLOADER := true
TARGET_NO_RADIOIMAGE := true
BOARD_KERNEL_BASE := 0x13600000
BOARD_KERNEL_PAGESIZE := 4096
TARGET_KERNEL_SOURCE := kernel/samsung/msm7x27

## Platform
TARGET_BOARD_PLATFORM := msm7x27
TARGET_ARCH := arm
TARGET_ARCH_VARIANT := armv6-vfp
TARGET_CPU_ABI := armeabi-v6l
TARGET_CPU_ABI2 := armeabi
TARGET_BOARD_PLATFORM_GPU := qcom-adreno200
TARGET_SPECIFIC_HEADER_PATH := device/samsung/msm7x27-common/include

## Webkit
ENABLE_WEBGL := true
TARGET_WEBKIT_USE_MORE_MEMORY := true
TARGET_FORCE_CPU_UPLOAD := true

## V8
JS_ENGINE := v8
HTTP := chrome

## JIT
WITH_JIT := true
ENABLE_JSC_JIT := true

## Camera
USE_CAMERA_STUB := false
COMMON_GLOBAL_CFLAGS += -DSAMSUNG_CAMERA_HARDWARE
BOARD_USE_NASTY_PTHREAD_CREATE_HACK := true

## Qualcomm, display
BOARD_USES_QCOM_HARDWARE := true
USE_OPENGL_RENDERER := true
BOARD_ADRENO_DECIDE_TEXTURE_TARGET := true
BOARD_EGL_CFG := device/samsung/msm7x27-common/prebuilt/lib/egl/egl.cfg
BOARD_NEEDS_MEMORYHEAPPMEM := true

COMMON_GLOBAL_CFLAGS += -DQCOM_HARDWARE -DREFRESH_RATE=60
COMMON_GLOBAL_CFLAGS += -DBINDER_COMPAT -DSAMSUNG_CAMERA_QCOM

## GPS
BOARD_VENDOR_QCOM_GPS_LOC_API_HARDWARE := msm7x27
BOARD_USES_QCOM_GPS := true
BOARD_VENDOR_QCOM_GPS_LOC_API_AMSS_VERSION := 1240

## Other Qualcomm config
BOARD_USES_QCOM_LIBS := true
BOARD_USES_QCOM_LIBRPC := true

## Bluetooth
BOARD_HAVE_BLUETOOTH := true
BOARD_HAVE_SAMSUNG_BLUETOOTH := true
BOARD_HAVE_BLUETOOTH_BCM := true

## FM
BOARD_HAVE_FM_RADIO := true
BOARD_GLOBAL_CFLAGS += -DHAVE_FM_RADIO
BOARD_FM_DEVICE := brcm2049

## Wi-Fi
BOARD_WPA_SUPPLICANT_DRIVER := AR6000
WPA_SUPPLICANT_VERSION := VER_0_6_X
BOARD_WLAN_DEVICE := wlan0
WIFI_DRIVER_MODULE_PATH := /system/wifi/ar6000.ko
WIFI_DRIVER_MODULE_NAME := ar6000

## RIL
TARGET_PROVIDES_LIBRIL := true
BOARD_MOBILEDATA_INTERFACE_NAME := "pdp0"

## UMS
TARGET_USE_CUSTOM_LUN_FILE_PATH := /sys/devices/platform/usb_mass_storage/lun0/file
BOARD_UMS_LUNFILE := "/sys/devices/platform/usb_mass_storage/lun0/file"

## Support for legacy touch screen
BOARD_USE_LEGACY_TOUCHSCREEN := true

## Audio
TARGET_PROVIDES_LIBAUDIO := true
BOARD_HAVE_SAMSUNG_AUDIO := true

## Custom lights module
TARGET_PROVIDES_LIBLIGHTS := true

## Samsung has weird framebuffer
TARGET_NO_INITLOGO := true

## Fixes colors in panorama
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
