Introduction
============

This project is the code that drives the [Rack Extension - Quick Start](https://pongasoft.com/re-quickstart/index.html) dynamic section.

This project also includes a conversion tool to convert an existing rack extension to one that uses `re-cmake` like the one you get after using Rack Extension - Quick Start. Check the [Rack Extension - Convert](./docs/convert.md) documentation.

This project is built with kotlin 1.4.10 and demonstrate a few interesting features

List of demonstrated features (kotlin)
--------------------------------------

* writing javascript code in kotlin
* integrating with an external library (jszip)
* load and generate a zip file
* dynamically generating images (both to be rendered in the page and to be added to the generated zip file)
* adding an event listener ("change", "click")
* posting a form via the "fetch" api and extracting/processing the json response (including error handling)
* replacing dom elements (React style)
* wrapping the javascript `iterator` api into kotlin `Iterator`
* use of `Promise`
* downloading a file via javascript

Build
=====

In development mode, simply run this command which automatically loads the web page in a browser window and listens to changes (in the IDEA you can select BrowserDevelopmentRun configuration):

```
./gradlew browserDevelopmentRun
```

For the production ready artifacts (under `build/distributions`)
```
./gradlew build
```

Note that the `deploy` task is being used locally to deploy only the necessary artifacts to their final destination prior to building the pongasoft website (which uses Jekyll). It can serve as an example to do something similar in your environment.

Release Notes
=============
#### 2024-10-08 - 1.6.1

- Fixes CMake deprecation warning

#### 2024-10-07 - 1.6.0

* Use RE SDK 4.5.0

#### 2024-06-19 - 1.5.2

* Added `device_categories` to `info.lua` after Reason 13 new requirements

#### 2023-09-23 - 1.5.1

* New versions of re-cmake

#### 2023-07-03 - 1.5.0

* Use RE SDK 4.4.0

#### 2022-02-01 - 1.2.5

* Introduced `GUI2D/gui_2D.lua`: extracted the list of images used into its own file so that it can be generated (ex: from re-edit)

#### 2022-02-01 - 1.2.6

* New versions of re-cmake / re-mock

#### 2022-02-01 - 1.2.4

* Fixes for Windows 10

#### 2022-01-30 - 1.2.3

* Added convert tool to convert an existing rack extension

#### 2022-01-24 - 1.2.2

* Use re-cmake 1.4.2 (use external re-logging)

#### 2022-01-22 - 1.2.0

* Use re-cmake 1.4.0 (comes with loguru so not included anymore)
* Use re-mock 1.0.0 for testing: instantiate and run the first batch (which is a very comprehensive test already)

#### 2021-10-28 - 1.1.9
* Uses re-cmake 1.3.7

#### 2021-10-26 - 1.1.8
* Uses re-cmake 1.3.6 / RE SDK 4.3.0 / Hi Res

#### 2021-10-02 - 1.1.7
* Uses re-cmake 1.3.5

#### 2021-09-27 - 1.1.6
* Uses re-cmake 1.3.4

#### 2021-09-27 - 1.1.5
* Fixed loguru universal45 build

#### 2021-09-23 - 1.1.4
* Fixed instrument plugin

#### 2021-07-11 - 1.1.3
* Use re-cmake 1.3.3 (testing improvement)

#### 2021-07-07 - 1.1.2
* Use re-cmake 1.3.2 (fixes issues with tests)

#### 2021-07-05 - 1.1.0
* Use static library (missing symbols when using dynamic one) 

#### 2021-07-04 - 1.0.6
* Use `re-cmake` 1.3.0 and added unit test to demonstrate new capability

#### 2021-01-25 - 1.0.5
* Updated the README file to emphasize the fact that `cmake` must be available on the command line

#### 2021-01-14 - 1.0.4
* Added `.gitignore` to exclude common files and folders
* Use 2.0 as the format for `motherboard_def.lua`

#### 2021-01-07 - 1.0.3
* Fetch `re-cmake` first (via `fetch-re-cmake.cmake`) in order to apply options before `project`
* Use `re-cmake` 1.2.0 to support building on new Apple chipset

#### 2020-11-19 - 1.0.2
* Use actual Reason Browser section name instead of enum name
* Use version from build file

#### 2020-11-14 - 1.0.1
* Fixed wrong date in zip file
* Open source release


#### 2020-11-12 - 1.0.0
* First release

Licensing
=========

* Apache 2.0 License. This project can be used according to the terms of the Apache 2.0 license.

* This project includes some stock images that come with the Rack Extension SDK and are used under the terms of the [Rack Extension SDK License](https://developer.reasonstudios.com/agreements/rack-extension-sdk-license-agreement)

