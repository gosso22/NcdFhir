plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.healthtracker.ncdcare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.healthtracker.ncdcare"
        minSdk = 26
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
        debug {
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    packaging { resources.excludes.addAll(listOf("META-INF/ASL-2.0.txt", "META-INF/LGPL-3.0.txt")) }
    kotlin { jvmToolchain(11) }
}

dependencies {

    implementation(platform(libs.androidx.compose.bom.v20250300))
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.datastore.preferences)
    androidTestImplementation(platform(libs.androidx.compose.bom.v20250300))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material3)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.kotlinx.coroutines.playservices)
    implementation(libs.play.services.location)

    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.ui.tooling)

    // UI Tests
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.engine)
    implementation(libs.data.capture)
    implementation(libs.material.v190)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat.v161)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.glide)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}