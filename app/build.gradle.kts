plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "com.ati11as.forestnavigator"
    compileSdk = 36
    defaultConfig { applicationId = "com.ati11as.forestnavigator"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0.0" }
    signingConfigs {
        create("release") {
            val ks = System.getenv("ANDROID_KEYSTORE_PATH")
            if (!ks.isNullOrBlank()) {
                storeFile = file(ks)
                storePassword = System.getenv("ANDROID_STORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }
    buildTypes { release { isMinifyEnabled = false; signingConfig = signingConfigs.getByName("release"); proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("org.osmdroid:osmdroid-android:6.1.20")
}
