plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.phonics_ta_in"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.phonics_ta_in"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

val copyDebugApk = tasks.register<Copy>("copyDebugApk") {
    description = "Copies the built debug APK to the app/debug directory"
    from(layout.buildDirectory.dir("outputs/apk/debug"))
    include("*.apk")
    into(layout.projectDirectory.dir("debug"))
}

val copyReleaseApk = tasks.register<Copy>("copyReleaseApk") {
    description = "Copies the built release APK to the app/release directory"
    from(layout.buildDirectory.dir("outputs/apk/release"))
    include("*.apk")
    into(layout.projectDirectory.dir("release"))
}

afterEvaluate {
    tasks.named("assembleDebug") {
        finalizedBy(copyDebugApk)
    }
    tasks.named("assembleRelease") {
        finalizedBy(copyReleaseApk)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}