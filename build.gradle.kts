plugins {
    kotlin("js") version "1.4.10"
}
group = "org.pongasoft"
version = "1.0-SNAPSHOT"

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
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
        }
    }
}

// Creates the zip file loaded at runtime (ends up in the build dir destinations folder)
val zipTask = tasks.create<Zip>("zip") {
    from("src/plugin/resources")
    include("**/*")
    archiveFileName.set("plugin.zip")
}

// Adds the destination folder so that the zip file can be loaded in dev mode */
kotlin.sourceSets.named("main") {
    resources.srcDir(zipTask.destinationDirectory)
}

// make sure the zip file is always built
tasks.findByName("processResources")?.dependsOn(zipTask)