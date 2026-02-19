plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0"
}

android {
    namespace = "com.a4a.g8invoicing"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.a4a.g8invoicing"
        minSdk = 26

        targetSdk = 36
        versionCode = 44
        versionName = "1.6.0-beta"

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

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
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

}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.2")

    // Compose BOM - aligns all Compose versions (Compose Multiplatform 1.7.3 ~ Jetpack Compose 1.7.5)
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.ui:ui")

    implementation("androidx.navigation:navigation-runtime-ktx:2.9.6")
    implementation("androidx.navigation:navigation-compose:2.9.6")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Koin (aligned with shared/ KMP version 4.0.2)
    implementation("io.insert-koin:koin-android:4.0.2")
    implementation("io.insert-koin:koin-androidx-compose:4.0.2")

    // Data Store (shared pref)
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // iText (create PDF)
    implementation("com.itextpdf:itext7-core:9.4.0")

    // fixing "android Missing class org.slf4j.impl.StaticLoggerBinder"
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("org.slf4j:slf4j-simple:2.0.17")


    // Drag & drop
    implementation("sh.calvin.reorderable:reorderable:3.0.0")

    //Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Popup
    implementation("androidx.datastore:datastore-preferences:1.2.0")
}