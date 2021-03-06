plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
}

repositories {
    google()
    jcenter()
    mavenCentral()
}

group = "saschpe.log4k"
version = "1.0.0"

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(17)
        targetSdkVersion(30)
    }

    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs("src/androidMain/kotlin")
            res.srcDirs("src/androidMain/res")
        }
    }

    lintOptions {
        isCheckReleaseBuilds = false
        isAbortOnError = false
    }

    testOptions.unitTests.isIncludeAndroidResources = true
}

kotlin {
    android {
        compilations.all { kotlinOptions.jvmTarget = "1.8" }
        publishAllLibraryVariants()
    }
    iosArm64 { binaries.framework("Log4K") }
    iosX64 { binaries.framework("Log4K") }
    js {
        browser()
        compilations.all {
            kotlinOptions.sourceMap = true
            kotlinOptions.moduleKind = "umd"
        }
    }
    jvm { compilations.all { kotlinOptions.jvmTarget = "1.8" } }

    sourceSets {
        named("commonMain") { }
        named("commonTest") {
            dependencies {
                implementation(kotlin("test-annotations-common"))
                implementation(kotlin("test-common"))
                implementation("io.mockk:mockk-common:1.10.2")
            }
        }

        named("androidMain") {
        }
        named("androidTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("androidx.test:core-ktx:1.3.0")
                implementation("androidx.test.ext:junit-ktx:1.1.2")
                implementation("io.mockk:mockk:1.10.2")
            }
        }

        val iosMain by creating {
            dependencies { }
        }
        val iosTest by creating {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        getByName("iosArm64Main") { dependsOn(iosMain) }
        getByName("iosArm64Test") { dependsOn(iosTest) }
        getByName("iosX64Main") { dependsOn(iosMain) }
        getByName("iosX64Test") { dependsOn(iosTest) }

        named("jsMain") { }
        // Note: mockk is not available for JavaScript
        named("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("io.mockk:mockk-dsl-js:1.10.2")
            }
        }

        named("jvmMain") { }
        named("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation("io.mockk:mockk:1.10.2")
            }
        }
    }

    tasks {
        register("universalFrameworkDebug", org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask::class) {
            baseName = "Log4K"
            from(
                iosArm64().binaries.getFramework("Log4K", "Debug"),
                iosX64().binaries.getFramework("Log4K", "Debug")
            )
            destinationDir = buildDir.resolve("bin/universal/debug")
            group = "Universal framework"
            description = "Builds a universal (fat) debug framework"
            dependsOn("linkLog4KDebugFrameworkIosArm64")
            dependsOn("linkLog4KDebugFrameworkIosX64")
        }

        register("universalFrameworkRelease", org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask::class) {
            baseName = "Log4K"
            from(
                iosArm64().binaries.getFramework("Log4K", "Release"),
                iosX64().binaries.getFramework("Log4K", "Release")
            )
            destinationDir = buildDir.resolve("bin/universal/release")
            group = "Universal framework"
            description = "Builds a universal (fat) release framework"
            dependsOn("linkLog4KReleaseFrameworkIosArm64")
            dependsOn("linkLog4KReleaseFrameworkIosX64")
        }

        register("universalFramework") {
            dependsOn("universalFrameworkDebug")
            dependsOn("universalFrameworkRelease")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "bintray"
            credentials {
                username = Secrets.Bintray.username
                password = Secrets.Bintray.password
            }
            url = uri("https://api.bintray.com/maven/saschpe/maven/log4k/;publish=1")
        }
    }
}
