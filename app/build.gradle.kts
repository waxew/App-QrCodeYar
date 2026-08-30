// تنظیمات ماژول اصلی Android.
// applicationId ثابت نگه داشته می‌شود تا نسخه‌های بعدی روی نسخه قبلی نصب شوند.
// Release production فقط زمانی با کلید خصوصی امضا می‌شود که متغیرهای محیطی QR_KEYSTORE_* تنظیم باشند.

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
}


fun envBuildString(name: String): String = (System.getenv(name) ?: "")
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

android {
    namespace = "com.waxew.qrbarcode"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.waxew.qrbarcode"
        minSdk = 23
        targetSdk = 35
        versionCode = 12
        versionName = "2.0.0"
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "SUPABASE_URL", "\"${envBuildString("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"${envBuildString("SUPABASE_PUBLISHABLE_KEY")}\"")
        buildConfigField("String", "DYNAMIC_QR_BASE_URL", "\"${envBuildString("DYNAMIC_QR_BASE_URL")}\"")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            val storeFilePath = System.getenv("QR_KEYSTORE_PATH")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = System.getenv("QR_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("QR_KEY_ALIAS")
                keyPassword = System.getenv("QR_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles("proguard-rules.pro")
            if (!System.getenv("QR_KEYSTORE_PATH").isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("com.google.zxing:core:3.5.3")

    implementation("androidx.camera:camera-core:1.5.1")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")

    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    kapt("androidx.room:room-compiler:2.8.4")

    implementation("com.android.billingclient:billing:9.1.0")
    implementation("androidx.biometric:biometric:1.1.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
