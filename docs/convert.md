Rack Extension - Convert
========================

Introduction
------------

The [Rack Extension - Quick Start](https://pongasoft.com/re-quickstart/index.html) project has been designed to create a brand-new rack extension using `re-cmake` for the build, `re-logging` for logging and `re-mock` for testing. This tool has been created to **convert** an existing rack extension to use `re-cmake` for the build, `re-logging` for logging and `re-mock` for testing.

> ### Note
> In the event only `re-cmake` is of interest (for the build), it is very easy to remove `re-logging` and `re-mock` (see below)

Requirements
------------

* RE SDK 4.1.0+ installed
* python3
* Tested on macOS 13 and Windows 11

Usage
-----

In order to run the conversion tool:

1. clone this repo locally. The path where the repo is cloned will be referred as `<path_to_re-quickstart>`.

    > ### Note
    > Do **not** clone this project inside the rack extension you want to convert!

2. cd to the root of the rack extension that needs to be converted (where `info.lua` lives)

3. Run the tool like this

    ```
    # on macOS
    > cd <path_to_re_to_convert>           # where info.lua lives
    > <path_to_re-quickstart>/convert.py   
    ```

    ```
    # on Windows
    > cd <path_to_re_to_convert>           # where info.lua lives
    > python3 <path_to_re-quickstart>\convert.py   
    ```

4. The tool then does its best guess at inferring a few things and ask for confirmation on a few topics. It then generates the appropriate files.

5. The project is now converted and can be compiled (and tested) using `re-cmake`

Example
-------

As a practical example, let's use the `VerySimpleSampler` example that comes with the RE SDK (under `SDK/Examples`). 

> ### Notes
> * The steps are similar for Windows (but you must use `python3` to invoke the python scripts and `re.bat` instead of `re.sh`).
> * On Windows, the file `StdInclude.h` needs to be modified to simply include `Jukebox.h` instead of the `#ifdef _MSC_VER / #endif` section

```
> cd <path_to_RE-SDK>/SDK/Examples/VerySimpleSampler
> <path_to_re-quickstart>/convert.py
### WARNING # WARNING # WARNING ###
This script will modify the content of the folder it is being run in [/tmp/copy-of-sdk/SDK/Examples/VerySimpleSampler].
It is strongly advised to run it in a copy of the original folder or better yet, in a fully committed version
controlled environment (so that it is easy to see the changes the script makes).
### WARNING # WARNING # WARNING ###

Which version of the RE SDK is the extension built with? (must be >= 4.1.0) (leave empty for default [4.4.0]) =
Could not locate the RE SDK in its default location [/Users/Shared/ReasonStudios/JukeboxSDK_4.4.0/SDK].
  Provide the path to the RE SDK: /Volumes/Vault/ReasonStudios/JukeboxSDK_440_229/SDK
Project Name (leave empty for default [VerySimpleSampler]) =
Is the device fully compliant with hi-res (4.3.0+)? If you have are not sure, answer no. (Y/n)? n
Name of the main instance this plugin creates (leave empty for default [CVerySimpleSampler]) =
Converting....
Done.

You can now run the following:
-------
> ./configure.py
> cd build
> ./re.sh install         # to build/install the plugin
> ./re.sh test -- -j 6    # to run the tests (-- -j 6 is to build in parallel)
-------
```

Let's go over each section:

### 1. WARNING
```
### WARNING # WARNING # WARNING ###
This script will modify the content of the folder it is being run in [/tmp/copy-of-sdk/SDK/Examples/VerySimpleSampler].
It is strongly advised to run it in a copy of the original folder or better yet, in a fully committed version
controlled environment (so that it is easy to see the changes the script makes).
### WARNING # WARNING # WARNING ###
```
First, the tool displays a warning message because the tool will modify the folder it is being run in. As a result it is strongly advised to run it in a copy of the original folder or better yet, in a fully committed version controlled environment

### 2. RE SDK Version
```
Which version of the RE SDK is the extension built with? (must be >= 4.1.0) (leave empty for default [4.4.0]) =
```
The first question is the version of the SDK that the rack extension should use (defaults to the latest one the tool is aware of). Note that the tool does not support versions prior to 4.1.0 simply because `re-cmake` does not support these versions.

### 3. Location of the RE SDK
```
Could not locate the RE SDK in its default location [/Users/Shared/ReasonStudios/JukeboxSDK_4.4.0/SDK].
  Provide the path to the RE SDK: /Volumes/Vault/ReasonStudios/JukeboxSDK_440_229/SDK
```
The tool tries to locate the RE SDK installation based on where `re-cmake` expects it by default (check [Note about the RE SDK location](https://github.com/pongasoft/re-cmake#note-about-the-re-sdk-location) section for more details on the default location). If not found, it prompts for the location.

> ### Note
> Make sure the path ends with the `SDK` folder as shown in the example. The tool will prompt again if the location is not found!

### 4. Project name
```
Project Name (leave empty for default [VerySimpleSampler]) =
```

The tool suggests a project name (the default is computed as the last part of the `product_id`). Feel free to change, but it must be a name that is compatible with a CMake project name.

### 4. HD support
```
Is the device fully compliant with hi-res (4.3.0+)? If you have are not sure, answer no. (Y/n)? n
```

This section only applies to SDK that supports HD (which is 4.3.0+) so the question won't be asked for previous SDKs. This section tries to determine the best option to run `RE2DRender` with. It is always safe to answer no, but it will make the build a bit slower. If the device is HD compliant, then the build can be a little faster. In this instance, the `VerySimpleSampler` example does not contain the HD version of the custom displays and as a result is not fully HD compliant.

> ### Note
> Answering "no" generates the following section in `cmake/options.cmake`, so it is easy to change it later to `hi-res-only` when the device is fully compliant.
>   ```cmake
>   #------------------------------------------------------------------------
>   # Option for invoking RE2DRender for hi res build
>   # Set to 'hi-res-only' by default. If the device does not fully support
>   # hi-res (no HD custom display background), set this option to 'hi-res'
>   #------------------------------------------------------------------------
>   set(RE_CMAKE_RE_2D_RENDER_HI_RES_OPTION "hi-res" CACHE STRING "Option for invoking RE2DRender for hi res build (hi-res or hi-res-only)")>
> ```

### 5. Main instance class name
```
Name of the main instance this plugin creates (leave empty for default [CVerySimpleSampler]) =
```

In order to generate the appropriate test, the name of the main instance is needed (this is the name of the class that gets instantiated in `JBox_Export_CreateNativeObject`).

> ### Note
> The test generated includes all `.h` files because it is hard to determine which one is the right one. Feel free to adjust accordingly.
 
### 6. Generation

Finally, the tool generates the needed files and displays a very brief summary of what commands to run.

### 7. Next Steps 

Once the plugin has been converted please follow the instructions below on how to build and deploy it: the "Ultra Quick Starting Guide" is the fastest and more condensed way to have the plugin up and running. Follow the "Starting Guide" instructions for more options. 

#### Ultra Quick Starting Guide

* Run `./configure.py` (resp. `python ./configure.py` for Win10)
* Now go into the `build` folder created by the `configure` script and run `./re.sh uninstall` (resp. `./re.bat uninstall` for Win10) to make sure that any prior build is uninstalled
* Now run `./re.sh install` (resp. `./re.bat install` for Win10)
* Open Recon and you should see the rack extension

Starting Guide
--------------

### Note about the RE SDK location

You can install the SDK wherever you want on your system and provide it as an argument to the `configure.py` script. Or you can install (or create a link) in a default location:

* `/Users/Shared/ReasonStudios/JukeboxSDK_<RE_SDK_VERSION>/SDK` for macOS
* `C:/Users/Public/Documents/ReasonStudios/JukeboxSDK_<RE_SDK_VERSION>/SDK` for Windows

Also, note that the `RE2DRender` program needs to be unzipped and is expected to be a sibling of `SDK` (can be changed as well).

During the conversion process, the proper location was determined and added to `cmake/options.cmake`.  

### Step 1 - Configure the build

Below, you will find several ways to configure the build. You can pick whichever you prefer:

- from the command line (using `configure.py`)
- using a GUI interface (`cmake-gui`)
- loading it directly in an IDE working natively with CMake (like CLion or Visual Studio Code)
- generating a project for Xcode or Visual Studio

#### Command line (`configure.py`)

Invoking the `configure.py` **creates** a `build` directory in the directory where the command is run. Although it is strongly recommended running this command outside the source tree, the `build` directory can be excluded in `.gitignore` and since everything is contained within the `build` folder it is easy to clean after the fact.

Running the `./configure.py -h` (resp `python configure.py -h` for Win10) command will print the usage.

Note that depending on how `python` is installed on your system you may have to invoke it differently.

Note that this script is expecting the `cmake` command line tool to be in the `PATH` (use `cmake -version` to confirm it is properly installed).

```
# python3 ./configure.py -h
usage: configure.py [-h] [-n] [-f] [-R] [-p RE_SDK_ROOT] [-r RE_2D_RENDER_ROOT] [-G GENERATOR] [-- <cmake_options>]
...
```
> #### Note
> The default generator for macOS is "Unix Makefiles" (not Xcode) because it works really well and creates very fast builds.

#### `cmake-gui`

If you are more comfortable with a GUI rather than a command line interface (like `configure.py`) you can simply use `cmake-gui`. After setting the generator, select `Grouped` and look for the properties in the `RE` group. The most important ones are

```
RE_SDK_ROOT
RE_2D_RENDER_ROOT
RE_2D_RENDER_EXECUTABLE
RE_RECON_EXECUTABLE
```

#### CLion (by Jetbrains)

CLion understand CMake natively. So simply open the project in CLion. Note that CLion creates a `cmake-build-debug` folder in the source tree (can be excluded in `.gitignore`) which can be changed under `Preferences / Build, Execution, Deployment / CMake / Generation Path`.

> #### Note
> On Windows, make sure that in the `Settings / Build, Execution, Deployment / Toolchains` you have `Visual Studio` selected with an architecture `amd64`!

#### Xcode (macOS)

To create an Xcode project simply use the  `python3 configure.py -G Xcode` flavor. This will generate a `build/<ProjectName>.xcodeproj` project that you can open directly in Xcode.

#### Visual Studio Code (Windows)

This section assumes that you have CMake Tools extension installed (a Microsoft extension)

1. Open Folder

   Open the project by opening the folder containing the plugin

2. Configure CMake

   Click on the CMake Icon in the toolbar then click on ‘Configure’ icon at which point you will be prompted for a Kit. You should select “Visual Studio Build Tools 2019 - amd64”. This will configure CMake.

> #### Tip
> Visual Studio Code should show a notification that states: _"CMake Tools would like to configure IntelliSense for this folder"_. It is strongly recommended allowing the action to proceed as the various includes (like the ones related to the RE SDK) will automatically be properly resolved.

#### Visual Studio (Windows)

To create a Visual Studio project simply use the command line first and invoke `python3 configure.py` (on Windows the default generator is Visual Studio). This will generate a `build/<ProjectName>.sln` solution that you can open directly in Visual Studio.

### Step 2 - Install the plugin

#### Command line - `re.sh install`

After running the `configure` script, simply go into the newly generated `build` folder and run `re.sh` (resp. `re.bat`)

Note that this script is expecting the `cmake` command line tool to be in the `PATH` (use `cmake -version` to confirm it is properly installed).

```
# cd build
# ./re.sh -h
usage: re.sh [-hnvlbdtRZ] <command> [<command> ...] [-- [native-options]]
...
```

You should be able to simply run `re.sh install` (resp. `re.bat`) to have the plugin fully built and installed in its default location. 

#### IDEs - select a target and build

Select the `native-install` target/configuration and build it (Ctrl-F9 with CLion).

> #### Tip
> After the `configure` phase, the `re.sh` (resp. `re.bat`) script will be available in the build folder, so you can also run any CLI command if you want even if you set up the project in the IDE.

### Step 3 - Try the plugin

You can then open Recon and load the rack extension.

> #### Note
> The plugin will **not** be accessible from Reason. You must use Recon. 

### Step 4 - Run the unit tests

#### Command line - `re.sh test`

Issuing this command will compile and run the unit tests (uses `re-mock`). This project provides a single basic test to demonstrate how to add new ones. Although the test provided is short, it actually does quite a lot of things: load and parse `info.lua`, `motherboard_def.lua` and `realtime_controller.lua` then instantiate the main instance class and run through one batch.

#### IDEs

Some IDEs (like CLion) are aware of unit testing natively (with Google Test) and offer the ability to run a specific test directly within the IDE. Otherwise, the target `native-run-test` will execute all of them.

Cleanup
-------

On purpose, this tool does not remove anything that is no longer necessary because it is best to be sure of the result prior to doing any cleanup. So here is a list of things that can be done afterwards:

### Don't want to use `re-logging` and `re-mock`?

If you are not planning to use `re-logging` and `re-mock` simply remove them from `CMakeLists.txt`

```cmake
# With re-logging and re-mock
# re_cmake_init(INCLUDES re-logging re-mock)

# Without re-logging and re-mock
re_cmake_init()

# Note:
# The variables re-logging_SOURCES, re-logging_INCLUDE_DIRS and re-mock_LIBRARY_NAME 
# are now empty so it is safe to leave them (in case you change your mind later) 
# But feel free to remove them from add_re_plugin(...)
```

> ### Note
> You should run `configure.py -f` after this change as CMake uses a caching mechanism and it needs to be purged.
 
### Removing unused files

- The `GUI` folder can be safely deleted since all the plugin files are in `GUI2D` and the `re-cmake` build framework takes care of generating the `GUI` folder outside of the source tree.
- The XCode project file (`*.xcodeproj`) can safely be removed since the XCode project can easily be created with the right generator (ex: `configure.py -G Xcode`).
- The Visual Studio files (`*.vcxproj`, etc...) can safely be removed since the Visual Studio project can easily be created with the right generator (run `cmake -G` to see the list of generators available).


More Information
----------------

The [re-cmake](https://github.com/pongasoft/re-cmake/blob/master/README.md) and [re-mock](https://github.com/pongasoft/re-mock/blob/master/docs/Quick_Start.md) documentations contains more information about these 2 projects.
