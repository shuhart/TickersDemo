plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.test.tickersdemo.network"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":core"))
    implementation(libs.coroutines.core)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
}
