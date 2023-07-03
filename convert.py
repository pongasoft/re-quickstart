#!/usr/bin/env python3

# Copyright (c) 2022 pongasoft
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
#
# @author Yan Pujante

import sys
import os
import subprocess
import platform

from string import Template
from pathlib import Path

if platform.system() == 'Darwin':
    script_name = 're.sh'
    apple = True
else:
    script_name = 're.bat'
    apple = False

# ask_user
def ask_user(msg, default):
    s = input(msg).strip()
    if len(s) == 0:
        return default if default else ask_user(msg, default)
    return s


# maybe_ask_user
def maybe_ask_user(option, msg, default):
    if option:
        return option
    else:
        if default:
            return ask_user(f'{msg} (leave empty for default [{default}]) = ', default)
        else:
            return ask_user(f'{msg} = ', default)


# confirm_user
def confirm_user(msg):
    s = input(msg).strip()
    if len(s) == 0:
        return True
    return s.lower() == 'y'


# maybe_confirm_user
def maybe_confirm_user(option, msg):
    if option:
        return option
    else:
        return confirm_user(f'{msg} (Y/n)? ')


plugin = dict()

# 1. determine various paths
this_script_root_dir = Path(os.path.dirname(os.path.realpath(sys.argv[0])))
re_project_dir = Path()
re_GUI2D_dir = re_project_dir / 'GUI2D'
blank_plugin_root = this_script_root_dir / 'src' / 'plugin' / 'resources' / 'skeletons' / 'common'

# 2. Check for info.lua (is the script run from the right location)
info_lua = re_project_dir / 'info.lua'
assert info_lua.exists(), \
    "No info.lua found. Make sure you run this command in the same directory where info.lua is located."

# Display warning
print(f'''### WARNING # WARNING # WARNING ###
This script will modify the content of the folder it is being run in [{re_project_dir.resolve()}].
It is strongly advised to run it in a copy of the original folder or better yet, in a fully committed version
controlled environment (so that it is easy to see the changes the script makes).
### WARNING # WARNING # WARNING ###
''')

# 3. Check for sdk
plugin['re_sdk_version'] = \
    maybe_ask_user(False, "Which version of the RE SDK is the extension built with? (must be >= 4.1.0)", '4.4.0')
re_sdk_version = tuple(map(int, (plugin['re_sdk_version'].split('.'))))
assert re_sdk_version[0] == 4 and re_sdk_version[1] >= 1, f"Version {plugin['re_sdk_version']} no supported."

# check for default sdk root based on version
if apple:
    default_re_sdk_root = \
        Path('/') / 'Users' / 'Shared' / 'ReasonStudios' / f'JukeboxSDK_{plugin["re_sdk_version"]}' / 'SDK'
else:
    default_re_sdk_root = \
        Path('C:') / 'Users' / 'Public' / 'Documents' / 'ReasonStudios'\
        / f'JukeboxSDK_{plugin["re_sdk_version"]}' / 'SDK'
re_sdk_root = default_re_sdk_root
version_txt = re_sdk_root / 'version.txt'

# ask user if not found
while not version_txt.exists():
    if re_sdk_root == default_re_sdk_root:
        user_sdk = ask_user(f'Could not locate the RE SDK in its default location [{re_sdk_root}]. '
                            'Provide the path to the RE SDK: ', False)
    else:
        user_sdk = ask_user(f'Path [{re_sdk_root}] is not a valid SDK. Provide the path to the RE SDK: ', False)
    re_sdk_root = Path(user_sdk)
    if re_sdk_root.parts[-1] != 'SDK':
        re_sdk_root = re_sdk_root / 'SDK'
    version_txt = re_sdk_root / 'version.txt'


def to_cmake_path(p):
    return "/".join(list(map(lambda x: x.replace('\\', ''), p.parts)))


if re_sdk_root == default_re_sdk_root:
    plugin['options_extras'] = ''
else:
    plugin['options_extras'] = f'''
# Using RE SDK from non default location (see re-cmake documentation)
set(RE_SDK_ROOT "{to_cmake_path(re_sdk_root)}" CACHE PATH "Location of RE SDK")     
'''

# 4. Load info.lua / extract project_name & device_type
if apple:
    lua_executable = re_sdk_root / 'Tools' / 'Build' / 'Lua' / 'Mac' / 'lua'
else:
    lua_executable = re_sdk_root / 'Tools' / 'Build' / 'Lua' / 'Win' / 'lua.exe'

info = subprocess.run([str(lua_executable), '-e', f"dofile('{info_lua}'); print(product_id .. ';' .. device_type)"],
                      text=True,
                      capture_output=True).stdout
info = str(info).split(';')
project_name = info[0].split('.')[-1]  # com.acme.XXX => XXX
device_type = info[1][:-1]  # remove the \n due to lua / print call

# Determine project name (for CMake)
plugin['cmake_project_name'] = maybe_ask_user(False, "Project Name", project_name)

# 5. Determine if the device handles samples
samples = list(re_project_dir.glob('Resources/**/*.wav')) + list(re_project_dir.glob('Resources/**/*.aiff'))
plugin['options_re_mock_support_for_audio_file'] = "ON" if len(samples) > 0 else "OFF"

# 6. Confirm if device supports hi res
if re_sdk_version[1] >= 3:
    hi_res = maybe_confirm_user(False, 'Is the device fully compliant with hi-res (4.3.0+)? '
                                       'If you have are not sure, answer no.')
    if not hi_res:
        plugin['options_extras'] += '''
#------------------------------------------------------------------------
# Option for invoking RE2DRender for hi res build
# Set to 'hi-res-only' by default. If the device does not fully support
# hi-res (no HD custom display background), set this option to 'hi-res'
#------------------------------------------------------------------------
set(RE_CMAKE_RE_2D_RENDER_HI_RES_OPTION "hi-res" CACHE STRING "Option for invoking RE2DRender for hi res build (hi-res or hi-res-only)")
'''

# 7. Determine cpp files & GUI2D files
re_sources_cpp = list(re_project_dir.glob('**/*.cpp'))
re_sources_h = list(re_project_dir.glob('**/*.h'))
re_sources_2d = list(re_GUI2D_dir.glob('*.png'))

assert len(re_sources_2d) > 0, "This tool only supports 2D (3D GUI is deprecated)."


tester_types = {
    'instrument': 'InstrumentTester',
    'creative_fx': 'CreativeEffectTester',
    'studio_fx': 'StudioEffectTester',
    'helper': 'HelperTester',
    'note_player': 'NotePlayerTester'
}

plugin['cmake_re_cpp_src_dir'] = "\"${CMAKE_CURRENT_LIST_DIR}\""
plugin['cmake_re_sources_cpp'] = "\n".join(
    list(map(lambda x: f'    "${{RE_CPP_SRC_DIR}}/{to_cmake_path(x)}"', re_sources_cpp)))
plugin['cmake_re_sources_2d'] = "\n".join(list(map(lambda x: f'    "${{RE_2D_SRC_DIR}}/{x.parts[1]}"', re_sources_2d)))
plugin['tester_device_type'] = tester_types[device_type]
plugin['test_includes'] = "\n".join(list(map(lambda x: f'#include <{to_cmake_path(x)}>', re_sources_h)))

plugin['test_class_name'] = maybe_ask_user(False, "Name of the main instance this plugin creates",
                                           f"C{plugin['cmake_project_name']}")

# class Processor
class Processor(Template):
    delimiter = '[-'
    pattern = r'''
    \[-(?:
       (?P<escaped>-) |            # Expression [-- will become [-
       (?P<named>[^\[\]\n-]+)-\] | # -, [, ], and \n can't be used in names
       \b\B(?P<braced>) |          # Braced names disabled
       (?P<invalid>)               #
    )
    '''


# process_file
def process_file(in_file_path, out_file_path, process):
    with open(in_file_path, 'r') as in_file:
        content = in_file.read()
        if process:
            content = Processor(content).substitute(plugin)
        with open(out_file_path, 'w') as out_file:
            out_file.write(content)
        os.chmod(out_file_path, os.stat(in_file_path).st_mode)


blank_plugin_files = {
    'unprocessed': {
        re_project_dir / 'cmake' / 're-cmake.cmake': blank_plugin_root / 'cmake' / 're-cmake.cmake',
        re_project_dir / 'configure.py': blank_plugin_root / 'configure.py',
    },

    'processed': {
        re_project_dir / 'cmake' / 'options.cmake': blank_plugin_root / 'cmake' / 'options.cmake',
        re_project_dir / 'test' / 'cpp' / 'test-Device.cpp': blank_plugin_root / 'test' / 'cpp' / 'test-Device.cpp',
        re_project_dir / 'CMakeLists.txt': blank_plugin_root / 'CMakeLists.txt',
        re_project_dir / 'GUI2D' / 'gui_2D.cmake': blank_plugin_root / 'GUI2D' / 'gui_2D.cmake',
    }
}

print("Converting....")

os.makedirs(re_project_dir / 'cmake', exist_ok=True)
os.makedirs(re_project_dir / 'test' / 'cpp', exist_ok=True)

for dst, src in blank_plugin_files['processed'].items():
    process_file(src, dst, True)


for dst, src in blank_plugin_files['unprocessed'].items():
    process_file(src, dst, False)

print('Done.')
if apple:
    print('''
You can now run the following:
-------
> ./configure.py
> cd build
> ./re.sh uninstall       # to remove any prior installation of the plugin
> ./re.sh install         # to build/install the plugin
> ./re.sh test -- -j 6    # to run the tests (-- -j 6 is to build in parallel)
-------
Check the https://github.com/pongasoft/re-quickstart/blob/master/docs/convert.md [Next Steps] section''')
else:
    print('''
You can now run the following:
-------
> python ./configure.py
> cd build
> ./re.bat uninstall       # to remove any prior installation of the plugin
> ./re.bat install         # to build/install the plugin
> ./re.bat test            # to run the tests
-------
Check the https://github.com/pongasoft/re-quickstart/blob/master/docs/convert.md [Next Steps] section''')
