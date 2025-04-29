plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.xdd"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.xdd"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("integer", "app_id", "2386079")
        resValue("string", "app_sign", "b2f34b82d905f322ccf6ea5e8c8e7efc7724de85bf4a9f7ac24cc903bdde063f")
    }

    buildFeatures {
        dataBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("libs/jtds-1.3.1.jar"))
    implementation(files("libs/mysql-connector-java-5.1.49.jar"))
    implementation(libs.firebase.firestore)
    implementation(libs.navigation.runtime)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.gridlayout)

    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:3.9.2")
    implementation("com.github.ZEGOCLOUD:zego_uikit_signaling_plugin_android:2.9.9")

    // ZEGO UIKit dependencias base
    implementation("com.github.ZEGOCLOUD:zego_uikit_android:3.5.8")
    implementation("com.github.ZEGOCLOUD:zego_express_engine_android:3.9.2")

    // Dependencias de Firebase
    implementation(platform(libs.firebase.bom)) // BoM para Firebase
    implementation(libs.firebase.auth) // Autenticación de Firebase
    implementation(libs.firebase.database) // Realtime Database

    // Google ML Kit
    implementation(libs.translate)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Agora
    implementation("io.agora.rtc:full-sdk:4.2.3")
}