plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.module"
    compileSdk = 36 // Can be modified as needed

    defaultConfig {
        applicationId = "com.example.module"
        minSdk = 31
        versionCode = 1
        versionName = "1.0.0"
    }

    lint {
        targetSdk = 36
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    lint {
        checkReleaseBuilds = false
    }

    dependenciesInfo {
        includeInApk = false
    }
}

dependencies {
    compileOnly("androidx.annotation:annotation:1.9.1")

    // 1. The Modern API (for XposedModule and @RegisterModule)
    compileOnly("io.github.libxposed:api:100.0.0")
    annotationProcessor("io.github.libxposed:processor:100.0.0")

    // 2. The Classic API (for XposedHelpers and XC_MethodHook)
    compileOnly("de.robv.android.xposed:api:82")
}

