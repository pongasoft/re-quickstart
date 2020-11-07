# Defines the location of the logging sources
set(LOGGING_CPP_SRC_DIR "${CMAKE_CURRENT_SOURCE_DIR}/logging")

# Defines the files to include for logging (they will be included in the Recon build ONLY)
set(logging_sources
    ${LOGGING_CPP_SRC_DIR}/logging.h
    ${LOGGING_CPP_SRC_DIR}/loguru.cpp
    ${LOGGING_CPP_SRC_DIR}/loguru.hpp
    )
