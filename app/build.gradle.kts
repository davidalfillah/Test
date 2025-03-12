plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id ("org.jetbrains.kotlin.plugin.serialization") version ("1.6.10")
}

android {
    namespace = "com.example.test"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.test"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "XENDIT_SECRET_KEY",
            "\"${project.findProperty("XENDIT_SECRET_KEY") ?: System.getenv("XENDIT_SECRET_KEY") ?: "FALLBACK_SECRET_KEY"}\""
        )

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
    // ✅ Tambahkan baris ini untuk mengaktifkan BuildConfig
    buildFeatures {
        buildConfig = true
        compose = true
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.compose.qr.code)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.androidx.emoji2.bundled)
    implementation(libs.androidx.emoji2.emojipicker)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.viewbinding)
    implementation (libs.ktor.client.okhttp)
    implementation (libs.algoliasearch.client.kotlin)
    implementation (libs.instantsearch.compose)
    implementation (libs.instantsearch.android)
    implementation (libs.instantsearch.insights.android)
    implementation (libs.instantsearch.android.paging3)
    implementation (libs.instantsearch.android.loading)
    implementation (libs.voice)
    implementation (libs.core)
    implementation (libs.xendit.android)
    implementation(libs.androidx.hilt.navigation.fragment)

    implementation (libs.androidx.media3.exoplayer)
    implementation (libs.androidx.media3.ui)
    implementation (libs.androidx.media3.common)

    implementation (libs.text.recognition)
    implementation (libs.text.recognition.chinese)
    implementation (libs.text.recognition.devanagari)
    implementation (libs.text.recognition.japanese)
    implementation (libs.text.recognition.korean)

    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.foundation.android)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.paging.compose.android)
    implementation(libs.androidx.runtime.livedata)

    implementation (libs.androidx.camera.core.v110beta01)
    implementation (libs.androidx.camera.camera2)
    implementation (libs.androidx.camera.lifecycle.v110beta01)
    implementation (libs.androidx.camera.video)
    implementation (libs.androidx.camera.view.v110beta01)
    implementation (libs.androidx.camera.extensions)

    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.views)
    implementation(libs.androidx.emoji2.views.helper)
    implementation (libs.play.services.base)
    implementation(libs.androidx.hilt.navigation.fragment.v100)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation (libs.gson)
    implementation(libs.androidx.navigation.compose)
    implementation (libs.snapper)
    implementation(libs.accompanist.systemuicontroller)
    implementation (libs.accompanist.navigation.animation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}