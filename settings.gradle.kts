plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

 pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "XposedModuleTemplate"
include(":app")

