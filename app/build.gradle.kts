plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nmm.objectdetectionapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nmm.objectdetectionapp"
        minSdk = 24
        targetSdk = 34
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
        viewBinding = true
        mlModelBinding = true
    }

}

dependencies {
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    val navVersion = "2.7.7" // creating a variable for navigation version



    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    //adding the dependencies for the navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")



    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // CameraX core library
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")
    implementation( "androidx.camera:camera-extensions:${cameraxVersion}")

    // Using CameraX for video recording needs the following dependency
//     implementation( "androidx.camera:camera-video:${cameraxVersion}")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.4.0")
    // TensorFlow Lite GPU (optional for faster processing)
    implementation("org.tensorflow:tensorflow-lite-gpu:2.4.0")
    // TensorFlow Lite support library
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")


    // Unit testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation ("org.mockito:mockito-core:3.+")
    testImplementation ("androidx.arch.core:core-testing:2.1.0")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:4.0.0")

    testImplementation ("org.mockito:mockito-inline:2.13.0") // For final class mocking

// Required for LiveData testing
    androidTestImplementation ("androidx.test.ext:junit:1.1.2")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.3.0")


}