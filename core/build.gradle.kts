plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.test.tickersdemo.core"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.gson)
}
