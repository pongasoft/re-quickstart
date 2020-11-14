Introduction
============

This project is the code that drives the [Rack Extension - Quick Start](https://pongasoft.com/re-quickstart/index.html) dynamic section.

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

#### 2020-11-14 - 1.0.1
* Fixed wrong date in zip file
* Open source release


#### 2020-11-12 - 1.0.0
* First release

License
=======

Apache 2.0

