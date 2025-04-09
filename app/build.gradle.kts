plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.xdd"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.xdd"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    // Agregar esta configuración para evitar errores de duplicación
//    packagingOptions {
//        pickFirst 'lib/armeabi-v7a/libc++_shared.so'
//        pickFirst 'lib/arm64-v8a/libc++_shared.so'
//        pickFirst 'lib/x86/libc++_shared.so'
//        pickFirst 'lib/x86_64/libc++_shared.so'
//    }
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