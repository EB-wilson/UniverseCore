cmake_minimum_required(VERSION 3.22)
project(jniSource)

include_directories(/lib/jvm/java-1.11.0-openjdk-arm64/include
        /lib/jvm/java-1.11.0-openjdk-arm64/include/linux)

set(CMAKE_CXX_STANDARD 14)

set(LIBRARY_OUTPUT_PATH ${PROJECT_SOURCE_DIR})

add_library(${PROJECT_NAME} SHARED universeCore_nativeImpl_FieldHandlerImpl.cpp)
