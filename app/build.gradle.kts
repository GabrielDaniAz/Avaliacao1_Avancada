plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.secrets)
    alias(libs.plugins.googleServices)
}

android {
    namespace = "br.com.gabrieldani.maps"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.com.gabrieldani.maps"
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(platform(libs.firebase.bom))
    implementation(libs.commons.math3)
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-database")
    implementation(project(":MathLibrary"))
    implementation(libs.gson)
    implementation(project(":CryptoLibrary"))
    implementation(project(":RegionLibrary"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}