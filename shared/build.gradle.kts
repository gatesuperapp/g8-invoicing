plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight") version "2.2.1"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

            // BigDecimal KMP - use api() to export to dependent modules
            api("com.ionspin.kotlin:bignum:0.3.10")

            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Compottie - Lottie for Compose Multiplatform
            implementation("io.github.alexzhirkevich:compottie:2.0.0-rc01")
            implementation("io.github.alexzhirkevich:compottie-resources:2.0.0-rc01")

            // SQLDelight - use api() to export to app module
            api("app.cash.sqldelight:coroutines-extensions:2.2.1")
        }

        androidMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
            // SQLDelight Android Driver
            implementation("app.cash.sqldelight:android-driver:2.2.1")
        }

        iosMain.dependencies {
            // SQLDelight Native Driver for iOS
            implementation("app.cash.sqldelight:native-driver:2.2.1")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.a4a.g8invoicing")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.a4a.g8invoicing.shared.resources"
    generateResClass = always
}

android {
    namespace = "com.a4a.g8invoicing.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
