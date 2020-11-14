import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js") version "1.4.10"
}
group = "org.pongasoft"
version = "1.0.1"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlinx")
    }
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
}
kotlin {
    js(IR) {
        browser {
            binaries.executable()
            webpackTask {
                outputFileName = "re-quickstart-${project.version}.js"
                cssSupport.enabled = true
            }
            runTask {
                outputFileName = "re-quickstart-${project.version}.js"
                cssSupport.enabled = true
            }
        }
    }
}

val pluginZipFilename = "plugin-${project.version}.zip"

// Creates the zip file loaded at runtime (we store it under buildDir/assets)
val zipTask = tasks.create<Zip>("zip") {
    from("src/plugin/resources")
    include("**/*")
    archiveFileName.set(pluginZipFilename)
    destinationDirectory.set(File(buildDir, "assets"))
}

// Adds the assets folder to the list of sources so that the zip file can be loaded in dev mode
kotlin.sourceSets.named("main") {
    resources.srcDir(zipTask.destinationDirectory)
}

// make sure the zip file is always built
tasks.findByName("processResources")?.dependsOn(zipTask)

// deploy task copies the relevant files to the website folder hosting it
val deployDir = File("/Volumes/Development/local/pongasoft-www/re-quickstart")

val kotlinWebpackTask = tasks.getByName<KotlinWebpack>("browserProductionWebpack")

tasks.create<Copy>("deploy") {
    from(kotlinWebpackTask.destinationDirectory)
    into(deployDir)
    include(pluginZipFilename, kotlinWebpackTask.outputFileName, "js/jszip.min.js")
}.dependsOn("build")