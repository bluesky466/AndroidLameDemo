#!/bin/zsh

NDK_ROOT=/Users/linjw/Library/Android/sdk/ndk/22.1.7171670
ANDROID_API_VERSION=21
NDK_TOOLCHAIN_ABI_VERSION=4.9
ABIS=(armeabi armeabi-v7a arm64-v8a x86 x86_64)

LAME_SOURCE_DIR=`pwd`
BUILD="$LAME_SOURCE_DIR/build"
OUTPUT="$LAME_SOURCE_DIR/output"

function make_standalone_toolchain()
{
  echo "make standalone toolchain --arch=${1} --api=${2} --install-dir=${3}"
  rm -rf ${3}

  $NDK_ROOT/build/tools/make_standalone_toolchain.py \
  --arch=${1} \
  --api=${2} \
  --install-dir=${3}
}

function export_vars()
{
    tool_chains_dir=${1}/bin
    export AR=${tool_chains_dir}/${2}-ar
    export AS=${tool_chains_dir}/${2}-as
    export NM=${tool_chains_dir}/${2}-nm
    export CC=${tool_chains_dir}/${2}-clang
    export CXX=${tool_chains_dir}/${2}-clang++
    export LD=${tool_chains_dir}/${2}-ld
    export RANLIB=${tool_chains_dir}/${2}-ranlib
    export STRIP=${tool_chains_dir}/${2}-strip
    export OBJDUMP=${tool_chains_dir}/${2}-objdump
    export OBJCOPY=${tool_chains_dir}/${2}-objcopy
    export ADDR2LINE=${tool_chains_dir}/${2}-addr2line
    export READELF=${tool_chains_dir}/${2}-readelf
    export SIZE=${tool_chains_dir}/${2}-size
    export STRINGS=${tool_chains_dir}/${2}-strings
    export ELFEDIT=${tool_chains_dir}/${2}-elfedit
    export GCOV=${tool_chains_dir}/${2}-gcov
    export GDB=${tool_chains_dir}/${2}-gdb
    export GPROF=${tool_chains_dir}/${2}-gprof
}

function configure_make_install()
{
    $LAME_SOURCE_DIR/configure \
            --enable-static \
            --disable-shared \
            --disable-frontend \
            --with-pic \
            --host=${1} \
            --prefix="$OUTPUT/${2}"
    make -j8 install

    rm -rf $OUTPUT/include
    mv $OUTPUT/${2}/include $OUTPUT

    rm -rf $OUTPUT/share
    mv $OUTPUT/${2}/share $OUTPUT

    mkdir -p $OUTPUT/lib/${2}
    cp $OUTPUT/${2}/lib/libmp3lame.a $OUTPUT/lib/${2}

    rm -r $OUTPUT/${2}

}
for ABI in $ABIS
do
    echo "building $ABI..."
    mkdir -p "$BUILD/$ABI"
    toolchains="$BUILD/toolchains"
    if [ $ABI = "armeabi" ]
    then
        make_standalone_toolchain arm $ANDROID_API_VERSION ${toolchains}
        export_vars ${toolchains} arm-linux-androideabi
        configure_make_install arm-linux-androideabi $ABI
    elif [ $ABI = "armeabi-v7a" ]
    then
        make_standalone_toolchain arm $ANDROID_API_VERSION ${toolchains}
        export_vars ${toolchains} arm-linux-androideabi
        configure_make_install arm-linux-androideabi $ABI
    elif [ $ABI = "arm64-v8a" ]
    then
        make_standalone_toolchain arm64 $ANDROID_API_VERSION ${toolchains}
        export_vars ${toolchains} aarch64-linux-android
        configure_make_install aarch64-linux-android $ABI
    elif [ $ABI = "x86" ]
    then
        make_standalone_toolchain x86 $ANDROID_API_VERSION ${toolchains}
        export_vars ${toolchains} i686-linux-android
        configure_make_install i686-linux-android $ABI
    elif [ $ABI = "x86_64" ]
    then
        make_standalone_toolchain x86_64 $ANDROID_API_VERSION ${toolchains}
        export_vars ${toolchains} x86_64-linux-android
        configure_make_install x86_64-linux-android $ABI
    else
        echo "abi [$ABI] is not support"
    fi
done
