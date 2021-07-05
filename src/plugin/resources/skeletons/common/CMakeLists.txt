cmake_minimum_required(VERSION 3.13)

# Using RE SDK version 4.2.0
set(RE_SDK_VERSION 4.2.0)

set(RE_CMAKE_ROOT_DIR "")
#set(RE_CMAKE_ROOT_DIR "${CMAKE_CURRENT_LIST_DIR}/../../pongasoft/re-cmake")

# If RE_CMAKE_ROOT_DIR is undefined/empty, it will fetch/download it from github
# If RE_CMAKE_ROOT_DIR is defined, it should point to the (local) folder where re-cmake is located
include(cmake/fetch_re-cmake.cmake)

# Default options provided by re-cmake
include("${RE_CMAKE_ROOT_DIR}/cmake/RECMakeOptions.cmake")

# Setting up project (note that the version is defined in lua.info)
project([-cmake_project_name-] LANGUAGES CXX)

# Using C++17
set(CMAKE_CXX_STANDARD 17)

# we include the framework itself
include("${RE_CMAKE_ROOT_DIR}/sdk.cmake")

# Including logging cmake file (loguru) for Recon build
include("cmake/logging.cmake")

# Defining where the sources of the plugin live
set(RE_CPP_SRC_DIR "${CMAKE_CURRENT_LIST_DIR}/src/cpp")

# Defining all the source files for the project
# Although you could use a file(GLOB ...) pattern, it is NOT recommended
# as the CMake generation would happen on every single build!
set(re_sources_cpp
    ${RE_CPP_SRC_DIR}/Device.h
    ${RE_CPP_SRC_DIR}/Device.cpp
    ${RE_CPP_SRC_DIR}/JukeboxExports.cpp
    )

# Defining where the local GUI2D files live
set(RE_2D_SRC_DIR "${CMAKE_CURRENT_LIST_DIR}/GUI2D")

# Defining all the 2D source files
# Although you could use a file(GLOB ...) pattern, it is NOT recommended
# as the CMake generation would happen on every single build!
set(re_sources_2d

    # lua files describing the GUI
    ${RE_2D_SRC_DIR}/device_2D.lua
    ${RE_2D_SRC_DIR}/hdgui_2D.lua

    # Images for the device
[-cmake-re_sources_2d-]
    )

# Location of the test cases
set(TEST_DIR "${CMAKE_CURRENT_LIST_DIR}/test/cpp")

# List of test cases
set(re_test_cpp
    "${TEST_DIR}/test-Device.cpp"
    )

# Adding the RE plugin
add_re_plugin(
    RE_SDK_VERSION       "${RE_SDK_VERSION}"
    RE_SDK_ROOT          "${RE_SDK_ROOT}"
    RE_2D_RENDER_ROOT    "${RE_2D_RENDER_ROOT}"
    BUILD_SOURCES        "${re_sources_cpp}"      # compiled for both local and jbox builds
    NATIVE_BUILD_SOURCES "${logging_sources}"     # compiled only for local builds
    RENDER_2D_SOURCES    "${re_sources_2d}"
    INCLUDE_DIRECTORIES  "${LOGGING_CPP_SRC_DIR}" # plugin uses logutu
    COMPILE_OPTIONS      -Wall
    ENABLE_DEBUG_LOGGING                          # turn on JBOX_TRACE and loguru
# Testing
    TEST_CASE_SOURCES        "${re_test_cpp}"     # the source files containing the test cases
    TEST_INCLUDE_DIRECTORIES "${RE_CPP_SRC_DIR}"  # tests can include plugin classes
    TEST_LINK_LIBS           "native-test-lib"    # tests can link plugin classes
)
