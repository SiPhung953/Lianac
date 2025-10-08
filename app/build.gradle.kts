plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "vn.edu.lianac"
    compileSdk = 36

    defaultConfig {
        applicationId = "vn.edu.lianac"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Material Design
//    implementation("com.google.android.material:material:1.12.0")
//    // AppCompat cho việc thay đổi theme
//    implementation("androidx.appcompat:appcompat:1.6.1")
    // Fragment
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.github.GrenderG:Toasty:1.5.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.cardview:cardview:1.0.0")
}