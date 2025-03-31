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

    lintOptions {
        disable ("Deprecated")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("libs\\jtds-1.3.1.jar"))
    implementation(files("libs\\mysql-connector-java-5.1.49.jar"))
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.gridlayout:gridlayout:1.0.0")

    // Dependencias de Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.2.3")) // BoM para Firebase
    implementation("com.google.firebase:firebase-auth") // Autenticación de Firebase
    implementation("com.google.firebase:firebase-database") // Realtime Database

    // Google ML Kit
    implementation("com.google.mlkit:translate:17.0.2")
}