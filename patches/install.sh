# This script automatically installs all patches needed for devices supported by Jellaxy
# Concept by Rashed97, reviewed by Bytecode, some minor updates by TheWhisp
# How to install: 1) cd ~/<patch>/<to>/<source>/ 2) ./device/samsung/msm7x27-common/patches/install.sh

echo "Obtaining build directory..."
rootdirectory="$PWD"
cd frameworks/base
echo "Applying frameworks/base patches..."
git am $rootdirectory/device/samsung/msm7x27-common/patches/frameworks_base/*.patch
cd $rootdirectory
cd packages/apps/Settings
echo "Applying packages/apps/Settings patches..."
git am $rootdirectory/device/samsung/msm7x27-common/patches/packages_apps_Settings/*.patch
cd $rootdirectory
cd bionic
echo "Applying bionic patches..."
git am $rootdirectory/device/samsung/msm7x27-common/patches/bionic/*.patch
cd $rootdirectory
cd system/netd
echo "Applying system/netd patches..."
git am $rootdirectory/device/samsung/msm7x27-common/patches/system_netd/*.patch
cd $rootdirectory
cd hardware/libhardware_legacy
echo "Applying hardware/libhardware_legacy patches..."
git am $rootdirectory/device/samsung/msm7x27-common/patches/libhardware_legacy/*.patch
cd $rootdirectory
