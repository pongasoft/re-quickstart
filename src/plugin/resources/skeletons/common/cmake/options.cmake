cmake_minimum_required(VERSION 3.24)

#################
# re-cmake
#################
set(re-cmake_GIT_TAG "v1.8.2" CACHE STRING "re-cmake git tag")
set(re-cmake_GIT_REPO "https://github.com/pongasoft/re-cmake" CACHE STRING "re-cmake git repository url")
set(re-cmake_DOWNLOAD_URL "${re-cmake_GIT_REPO}/archive/refs/tags/${re-cmake_GIT_TAG}.zip" CACHE STRING "re-cmake download url" FORCE)
set(re-cmake_DOWNLOAD_URL_HASH "SHA256=0d57ad9ab5b9142d5054d3f6d746e949a8ddca8776afec46e0561a6db6afe95e" CACHE STRING "re-cmake download url hash" FORCE)

# Uncomment/set to proper value if local instead of remote
set(re-cmake_ROOT_DIR "") # use remote re-cmake
#set(re-cmake_ROOT_DIR "${RE_PROJECT_ROOT_DIR}/../../pongasoft/re-cmake")

#################
# re-logging
#################
set(re-logging_GIT_REPO "https://github.com/pongasoft/re-logging" CACHE STRING "re-logging git repository url")
set(re-logging_GIT_TAG "v2.0.0" CACHE STRING "re-logging git tag")
set(re-logging_DOWNLOAD_URL "${re-logging_GIT_REPO}/archive/refs/tags/${re-logging_GIT_TAG}.zip" CACHE STRING "re-logging download url" FORCE)
set(re-logging_DOWNLOAD_URL_HASH "SHA256=0de4ffda5f761187abe0eb3f205a4f928177e0d6838f5f9111615de4e9ef1d56" CACHE STRING "re-logging download url hash" FORCE)

# Uncomment/set to proper value if local instead of remote
set(re-logging_ROOT_DIR "") # use remote re-logging
#set(re-logging_ROOT_DIR "${RE_PROJECT_ROOT_DIR}/../../pongasoft/re-logging")

#################
# re-mock
#################
set(re-mock_GIT_REPO "https://github.com/pongasoft/re-mock" CACHE STRING "re-mock git repository url")
set(re-mock_GIT_TAG "v1.8.0" CACHE STRING "re-mock git tag")
set(re-mock_DOWNLOAD_URL "${re-mock_GIT_REPO}/archive/refs/tags/${re-mock_GIT_TAG}.zip" CACHE STRING "re-mock download url" FORCE)
set(re-mock_DOWNLOAD_URL_HASH "SHA256=084a88445b1d3315359c09dd5dbc852801f095b6c1eafdeffc8fc5c88ccbcce4" CACHE STRING "re-mock download url hash" FORCE)

# Uncomment/set to proper value if local instead of remote
set(re-mock_ROOT_DIR "") # use remote re-mock
#set(re-mock_ROOT_DIR "${RE_PROJECT_ROOT_DIR}/../../pongasoft/re-mock")

#################
# googletest
#################
set(googletest_GIT_REPO "https://github.com/google/googletest" CACHE STRING "googletest git repository URL")
set(googletest_GIT_TAG "v1.17.0" CACHE STRING "googletest git tag")
set(googletest_DOWNLOAD_URL "${googletest_GIT_REPO}/archive/refs/tags/${googletest_GIT_TAG}.zip" CACHE STRING "googletest download url" FORCE)
set(googletest_DOWNLOAD_URL_HASH "SHA256=40d4ec942217dcc84a9ebe2a68584ada7d4a33a8ee958755763278ea1c5e18ff" CACHE STRING "googletest download url hash" FORCE)

# Uncomment/set to proper value if local instead of remote
set(googletest_ROOT_DIR "") # use remote googletest
#set(re-googletest_ROOT_DIR "${RE_PROJECT_ROOT_DIR}/../../google/googletest")

[-options_extras-]