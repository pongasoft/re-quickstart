cmake_minimum_required(VERSION 3.13)

include(FetchContent)

if(RE_CMAKE_ROOT_DIR)
  # instructs FetchContent to not download or update but use the location instead
  set(FETCHCONTENT_SOURCE_DIR_RE-CMAKE ${RE_CMAKE_ROOT_DIR})
else()
  set(FETCHCONTENT_SOURCE_DIR_RE-CMAKE "")
endif()

set(RE_CMAKE_GIT_REPO "https://github.com/pongasoft/re-cmake" CACHE STRING "re-cmake git repository url")
set(RE_CMAKE_GIT_TAG "v1.3.3" CACHE STRING "re-cmake git tag")

FetchContent_Declare(re-cmake
      GIT_REPOSITORY    ${RE_CMAKE_GIT_REPO}
      GIT_TAG           ${RE_CMAKE_GIT_TAG}
      GIT_CONFIG        advice.detachedHead=false
      GIT_SHALLOW       true
      SOURCE_DIR        "${CMAKE_CURRENT_BINARY_DIR}/re-cmake"
      BINARY_DIR        "${CMAKE_CURRENT_BINARY_DIR}/re-cmake-build"
      CONFIGURE_COMMAND ""
      BUILD_COMMAND     ""
      INSTALL_COMMAND   ""
      TEST_COMMAND      ""
      )

FetchContent_GetProperties(re-cmake)

if(NOT re-cmake_POPULATED)

  if(FETCHCONTENT_SOURCE_DIR_RE-CMAKE)
    message(STATUS "Using re-cmake from local ${FETCHCONTENT_SOURCE_DIR_RE-CMAKE}")
  else()
    message(STATUS "Fetching re-cmake ${RE_CMAKE_GIT_REPO}/tree/${RE_CMAKE_GIT_TAG}")
  endif()

  FetchContent_Populate(re-cmake)

endif()

set(RE_CMAKE_ROOT_DIR ${re-cmake_SOURCE_DIR})
