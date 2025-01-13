plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.dsk.musicbuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dsk.musicbuddy"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.ui.desktop)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (libs.androidx.cardview)
    implementation (libs.androidx.recyclerview)
    implementation (libs.material)

    //    To fit all screen sizes
    implementation (libs.sdp.android)
//    Ads
    implementation (libs.play.services.ads)

    ksp(libs.androidx.room.compiler)

    // LiveData
    implementation(libs.androidx.lifecycle.livedata)
    // Annotation processor
    annotationProcessor(libs.androidx.lifecycle.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.extensions)
    implementation (libs.androidx.lifecycle.runtime.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)

    implementation (libs.androidx.fragment.ktx)

    implementation (libs.gson)

    implementation (libs.androidx.coordinatorlayout)

    implementation (libs.androidx.viewpager2)

    implementation (libs.androidx.media)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)

    // Room Database dependencies
    implementation (libs.androidx.room.runtime)
    ksp (libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)

}