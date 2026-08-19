import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21"
}

// Release signing credentials, loaded from keystore.properties at the repo root.
// The file is gitignored; each machine keeps its own local copy.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.a4a.g8invoicing"
    compileSdk = 37

    flavorDimensions += "channel"
    productFlavors {
        create("stable") {
           // main app
        }
        create("beta") {
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-beta"
        }
    }

    defaultConfig {
        applicationId = "com.a4a.g8invoicing"
        minSdk = 26

        targetSdk = 36
        versionCode = 60
        versionName = "1.8.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = keystoreProperties.getProperty("storeFile")
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            // Match the opt-in configured in shared/build.gradle.kts — Koin definitions
            // in the app module reference SubscriptionRepository whose constructor takes
            // a kotlin.time.Clock (still @ExperimentalTime in stdlib 2.3.x).
            optIn.add("kotlin.time.ExperimentalTime")
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
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.activity:activity-compose:1.13.0")

    // Compose BOM - aligns all Compose versions (Compose Multiplatform 1.7.3 ~ Jetpack Compose 1.7.5)
    implementation(platform("androidx.compose:compose-bom:2025.06.01"))
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.ui:ui")

    implementation("androidx.navigation:navigation-runtime-ktx:2.9.6")
    implementation("androidx.navigation:navigation-compose:2.9.6")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.8.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.06.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Koin (aligned with shared/ KMP version 4.2.2)
    implementation("io.insert-koin:koin-android:4.2.2")
    implementation("io.insert-koin:koin-androidx-compose:4.2.2")

    // Encrypted token storage
    implementation("androidx.security:security-crypto:1.1.0")

    // Data Store (shared pref)
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // iText (create PDF)
    implementation("com.itextpdf:itext7-core:9.5.0")

    // fixing "android Missing class org.slf4j.impl.StaticLoggerBinder"
    implementation("org.slf4j:slf4j-api:2.0.18")
    implementation("org.slf4j:slf4j-simple:2.0.18")


    // Drag & drop
    implementation("sh.calvin.reorderable:reorderable:3.1.0")

    //Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Popup
    implementation("androidx.datastore:datastore-preferences:1.2.1")
}