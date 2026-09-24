plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.QuickDrop"
    compileSdk = 34




    defaultConfig {
        applicationId = "com.example.QuickDrop"
        minSdk = 23


        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("androidx.core:core-ktx:1.10.1")

    implementation ("androidx.cardview:cardview:1.0.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation ("com.squareup.picasso:picasso:2.71828")


    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.android.volley:volley:1.2.0")

    implementation ("com.github.bumptech.glide:glide:4.14.2")
    // Skip this if you don't want to use integration libraries or configure Glide.
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")

    implementation ("com.nostra13.universalimageloader:universal-image-loader:1.9.5")

    // Open-source, free replacement for the Google Maps SDK / Directions / Places APIs.
    // No API key required. See core/map/ for the wrapper classes.
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    implementation ("io.reactivex.rxjava2:rxjava:2.2.4")
    implementation ("io.reactivex.rxjava2:rxandroid:2.1.0")

    implementation ("com.sothree.slidinguppanel:library:3.4.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.5.1")
    implementation ("androidx.lifecycle:lifecycle-livedata:2.5.1")
}
