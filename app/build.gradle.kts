import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    kotlin("plugin.serialization") version "1.9.24"
    id("kotlin-parcelize")
}
val properties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val facebookAppId: String = properties.getProperty("facebook_app_id", "FAKE_FB_ID")
val facebookClientToken: String = properties.getProperty("facebook_client_token", "FAKE_FB_TOKEN")
val fbLoginProtocolScheme: String = properties.getProperty("fb_login_protocol_scheme", "fbFAKE")

android {


    namespace = "com.ltcn272.finny"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ltcn272.finny"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["facebook_app_id"] = facebookAppId
        manifestPlaceholders["facebook_client_token"] = facebookClientToken
        manifestPlaceholders["fb_login_protocol_scheme"] = fbLoginProtocolScheme
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            pickFirsts += "META-INF/io.netty.versions.properties"

            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/*.SF"
            excludes += "META-INF/*.DSA"
            excludes += "META-INF/*.RSA"
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.googleid)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.compose.material)

    // Hilt (KSP)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.nav.fragment)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.google.firebase.messaging)

    // Google Sign-In
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)


    // Network
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logging.interceptor)

    // Lottie
    implementation(libs.lottie.compose)

    // Facebook Login
    implementation(libs.facebook.android.sdk)
    implementation(libs.facebook.login)

    // Calendar
    implementation(libs.calendar.compose )

    // Paging Compose
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)


    // wheel date time picker
    implementation(libs.wheelpickercompose)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Coil
    implementation(libs.coil.compose)

    // Preferences DataStore for locale persistence
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.switchy.compose)

    // Splash
    implementation(libs.androidx.core.splashscreen)
}