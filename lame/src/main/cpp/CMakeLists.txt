# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)


project("lame")

add_library(
        lame
        SHARED
        native-lib.cpp)

# 导入lame头文件
include_directories(${CMAKE_SOURCE_DIR}/include)

#导入lame静态库
add_library(mp3lame STATIC IMPORTED)
set_target_properties(
        mp3lame
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../../../jniLibs/${ANDROID_ABI}/libmp3lame.a)

find_library(
        log-lib
        log)

target_link_libraries(
        lame
        mp3lame # 链接lame静态库
        ${log-lib})