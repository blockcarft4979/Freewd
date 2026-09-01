import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.Base64
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.2.10"
    id("kotlin-parcelize")
}


android {
    namespace = "com.freewdcmkt.bck"
    compileSdk = 37

    signingConfigs {
        create("release") {

            val isCI = System.getenv("CI") == "true"

            if (isCI) {

                val base64 = System.getenv("KEYSTORE_BASE64")
                    ?: error("❌ KEYSTORE_BASE64 is missing in CI environment!")
                val decoded = Base64.getDecoder().decode(base64)
                val keystoreFile = file("keystore.jks")
                keystoreFile.writeBytes(decoded)
                storeFile = keystoreFile

                storePassword = System.getenv("KEYSTORE_PASSWORD")
                    ?: error("❌ KEYSTORE_PASSWORD is missing!")
                keyAlias = System.getenv("KEY_ALIAS")
                    ?: error("❌ KEY_ALIAS is missing!")
                keyPassword = System.getenv("KEY_PASSWORD")
                    ?: error("❌ KEY_PASSWORD is missing!")
            } else {

                println("🔧 Local build: using debug signing (no keystore needed)")
            }
        }
    }

    defaultConfig {
        applicationId = "com.freewdcmkt.bck"
        minSdk = 26
        targetSdk = 36
        versionCode = SimpleDateFormat("yyMMddHH",Locale.getDefault()).format(Date()).toInt()
        versionName = "${SimpleDateFormat("yy.MM.dd.HH",Locale.getDefault()).format(Date())}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            versionNameSuffix = " TEST VERSION"
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.mikepenz:multiplatform-markdown-renderer:0.32.0")
    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.32.0")
    implementation("com.mikepenz:multiplatform-markdown-renderer-coil2:0.32.0")
    implementation("io.coil-kt.coil3:coil:3.1.0")
    implementation("io.coil-kt.coil3:coil-svg:3.1.0")
    implementation("io.coil-kt.coil3:coil-compose:3.5.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("com.squareup.okhttp3:okhttp:5.4.0")
    implementation("androidx.paging:paging-compose:3.2.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.graphics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}