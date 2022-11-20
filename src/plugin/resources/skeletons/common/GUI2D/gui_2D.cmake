# Defining all the 2D source files
# Although you could use a file(GLOB ...) pattern, it is NOT recommended
# as the CMake generation would happen on every single build!
set(re_sources_2d

    # lua files describing the GUI
    "${RE_2D_SRC_DIR}/device_2D.lua"
    "${RE_2D_SRC_DIR}/hdgui_2D.lua"

    # Images for the device
[-cmake_re_sources_2d-]
    )
