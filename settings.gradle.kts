pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.github.com/rovo89/XposedBridge")
    }
}

rootProject.name = "XposedModuleTemplate"
include(":app")
includeBuild("libxposed-api")
