plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // ❌ REMOVED: alias(libs.plugins.kotlinCompose) - This is for Jetpack Compose
}

android {
    namespace = "com.example.cattler"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cattler"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        // ✅ This is correct.
        viewBinding = true

        // ❌ This is now false.
        compose = false
    }
}

dependencies {
    // --- Core Android Libraries (Kept) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity)

    // --- Libraries for our XML/View-based UI (Kept) ---
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.material)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // --- ❌ ALL JETPACK COMPOSE DEPENDENCIES REMOVED ---
    // implementation(platform("androidx.compose:compose-bom:...")), etc.

    // --- Networking (Kept) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // --- Charting (Kept) ---
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // --- Firebase for Notifications (Kept) ---
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    // --- Testing (Kept) ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}