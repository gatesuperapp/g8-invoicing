plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("app.cash.sqldelight") version "2.0.0"
}

android {
    namespace = "com.a4a.g8invoicing"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.a4a.g8invoicing"
        minSdk = 26
        targetSdk = 34
        versionCode = 24
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.md",
                "META-INF/LICENSE-notice.md",
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/native-image/reflect-config.json",
                "META-INF/native-image/resource-config.json"
            )
        )
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.material3:material3:1.2.0-alpha10")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.7")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.7")
    implementation("androidx.navigation:navigation-runtime-ktx:2.7.7")
    implementation("androidx.wear.compose:compose-foundation:1.4.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // hilt
    implementation("com.google.dagger:hilt-android:2.49")
    kapt("com.google.dagger:hilt-compiler:2.49")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //SQLDelight
    implementation("app.cash.sqldelight:android-driver:2.0.0")
    implementation("app.cash.sqldelight:coroutines-extensions-jvm:2.0.0")

/*    // Leak Canary (memory leaks)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")*/

    // Data Store (shared pref)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // iText (create PDF)
    implementation("com.itextpdf:itext7-core:7.2.6")

    // fixing "android Missing class org.slf4j.impl.StaticLoggerBinder"
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("org.slf4j:slf4j-simple:2.0.13")

    // Auth info storage
    implementation("androidx.security:security-crypto:1.0.0")
    implementation("org.mindrot:jbcrypt:0.4")

    // Lottie
    implementation("com.airbnb.android:lottie-compose:4.0.0")

}