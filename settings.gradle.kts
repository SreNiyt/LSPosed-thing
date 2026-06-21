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
        // The official, active repository for the classic Xposed API
        maven { url = uri("https://api.xposed.info/") }
    }
}

rootProject.name = "XposedModuleTemplate"
include(":app")

