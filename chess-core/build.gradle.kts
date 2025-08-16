plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.timur.chessiq.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        // library modülünde targetSdk kullanmıyoruz (AGP 9 ile uyarı veriyor)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // (Şimdilik çekirdek için harici bağımlılık yok)

    // --- Test bağımlılıkları ---
    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
