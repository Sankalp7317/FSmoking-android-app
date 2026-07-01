plugins {
    id("com.android.application")
}

android {
    namespace = "com.fsmoking.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fsmoking.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core-ktx:1.12.0")

    // Lifecycle / ViewModel / LiveData (MVVM)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.7.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // WorkManager (notifications)
    implementation("androidx.work:work-runtime:2.9.0")

    // RecyclerView (history list)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
}