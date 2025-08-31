plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.vampire_system"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.vampire_system"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

kapt {
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
    }
}

dependencies {
    implementation(libs.lifecycle.runtime)

    // Material Design & UI Polish
    implementation(libs.material)
    implementation(libs.splashscreen)
    implementation(libs.timber)

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")

    // DataStore
    implementation(libs.datastore)

    // WorkManager
    implementation(libs.work.runtime)

    // UI basics
    implementation(libs.fragment.ktx)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.documentfile)

    // CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.video)

    // Media & images
    implementation(libs.exoplayer)
    implementation(libs.coil)
    implementation(libs.coil.video) // enables video frame thumbs
    implementation(libs.paging)

    // Serialization
    implementation(libs.serialization.json)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    add("kapt", libs.room.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.espresso.core)
    
    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}