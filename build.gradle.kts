import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    kotlin("js") version "1.8.21"
    id("com.github.gmazzo.buildconfig") version "2.0.2"
}

group = "org.pongasoft"
version = "1.5.0"

repositories {
    mavenCentral()
    jcenter()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-html:0.7.2")
}

buildConfig {
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    useKotlinOutput()
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            webpackTask {
                outputFileName = "re-quickstart-${project.version}.js"
            }
            runTask {
                outputFileName = "re-quickstart-${project.version}.js"
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

val generateBuildConfig : com.github.gmazzo.gradle.plugins.BuildConfigTask by tasks

// Adds the assets folder to the list of sources so that the zip file can be loaded in dev mode
kotlin.sourceSets.named("main") {
    kotlin.srcDir(generateBuildConfig.outputDir)
    resources.srcDir(zipTask.destinationDirectory)
}

tasks.findByName("compileKotlinJs")?.dependsOn(generateBuildConfig)

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