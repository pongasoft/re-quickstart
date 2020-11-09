plugins {
    kotlin("js") version "1.4.10"
}
group = "org.pongasoft"
version = "1.0.0"

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

// Creates the zip file loaded at runtime (we store it under buildDir/assets)
val zipTask = tasks.create<Zip>("zip") {
    from("src/plugin/resources")
    include("**/*")
    archiveFileName.set("plugin-${project.version}.zip")
    destinationDirectory.set(File(buildDir, "assets"))
}

// Adds the assets folder to the list of sources so that the zip file can be loaded in dev mode
kotlin.sourceSets.named("main") {
    resources.srcDir(zipTask.destinationDirectory)
}

// make sure the zip file is always built
tasks.findByName("processResources")?.dependsOn(zipTask)