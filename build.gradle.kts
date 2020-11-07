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

// Creates the zip file loaded at runtime (ideally it would be generated in the build dir, but I have no clue
// how to add it to the browserDevelopmentRun input files) so for now it is being generated in the source tree
val zipTask = tasks.create<Zip>("zip") {
    from("src/plugin/resources")
    include("**/*")
    archiveFileName.set("plugin.zip")
    destinationDirectory.set(File("src/main/resources"))
}

tasks.findByName("processResources")?.dependsOn(zipTask)