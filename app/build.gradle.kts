plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Tambahkan plugin Google Services
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.kostfinder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kostfinder"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation("androidx.compose.runtime:runtime")
    implementation(libs.androidx.material3)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.foundation:foundation:1.6.8")
    implementation (libs.accompanist.navigation.animation)

    // Firebase BoM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")
    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")
    // Firebase Storage
    implementation("com.google.firebase:firebase-storage-ktx")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}