plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight") version "2.2.1"
    kotlin("plugin.serialization") version "2.2.0"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    jvm("desktop") {
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
        // Create jvmMain source set shared between Android and Desktop
        val jvmMain by creating {
            dependsOn(commonMain.get())
        }

        commonMain.dependencies {
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

            // BigDecimal KMP - use api() to export to dependent modules
            api("com.ionspin.kotlin:bignum:0.3.10")

            // Multiplatform Settings (for language persistence + token storage)
            // api() so the app module can build SharedPreferencesSettings(EncryptedSharedPreferences)
            api("com.russhwolf:multiplatform-settings-no-arg:1.1.1")

            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            // api() so the app module can call getString(Res.string.…) / stringResource(Res.string.…)
            // for strings that were migrated from app/src/main/res/ to the shared source.
            api(compose.components.resources)

            // Compottie - Lottie for Compose Multiplatform
            implementation("io.github.alexzhirkevich:compottie:2.0.0-rc01")
            implementation("io.github.alexzhirkevich:compottie-resources:2.0.0-rc01")

            // Reorderable - Drag and drop for LazyColumn/LazyRow (KMP compatible)
            implementation("sh.calvin.reorderable:reorderable:3.0.0")

            // Navigation Compose Multiplatform (JetBrains)
            api("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")

            // Lifecycle ViewModel Compose Multiplatform (JetBrains)
            api("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
            // Lifecycle Runtime Compose — exposes LifecycleEventEffect / LocalLifecycleOwner
            // in commonMain (needed for ON_RESUME auto-refresh in the Account screen).
            api("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

            // Koin for KMP
            api("io.insert-koin:koin-core:4.0.2")
            api("io.insert-koin:koin-compose:4.0.2")
            api("io.insert-koin:koin-compose-viewmodel:4.0.2")

            // Ktor Client (HTTP) — api() so the app module can configure HttpClient
            api("io.ktor:ktor-client-core:2.3.12")
            api("io.ktor:ktor-client-content-negotiation:2.3.12")
            api("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
            api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

            // SQLDelight - use api() to export to app module
            api("app.cash.sqldelight:coroutines-extensions:2.2.1")
        }

        jvmMain.dependencies {
            // iText for PDF generation (shared between Android and Desktop)
            implementation("com.itextpdf:itext7-core:9.4.0")
        }

        androidMain {
            dependsOn(jvmMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
                // SQLDelight Android Driver
                implementation("app.cash.sqldelight:android-driver:2.2.1")
                // AppCompat for locale management
                implementation("androidx.appcompat:appcompat:1.7.0")
                // Ktor engine Android — api() so the app module can use OkHttp engine
                api("io.ktor:ktor-client-okhttp:2.3.12")
                // Encrypted storage for tokens
                implementation("androidx.security:security-crypto:1.1.0-alpha06")
            }
        }

        iosMain.dependencies {
            // SQLDelight Native Driver for iOS
            implementation("app.cash.sqldelight:native-driver:2.2.1")
            // Ktor engine iOS
            implementation("io.ktor:ktor-client-darwin:2.3.12")
        }

        val desktopMain by getting {
            dependsOn(jvmMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
                // SQLDelight JVM Driver for Desktop
                implementation("app.cash.sqldelight:sqlite-driver:2.2.1")
                // Ktor engine Desktop
                implementation("io.ktor:ktor-client-cio:2.3.12")
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
        }
    }
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("com.a4a.g8invoicing")
            // Pin the expected schema as a checked-in <version>.db file. Run
            // `./gradlew :shared:generateCommonMainDatabaseSchema` after bumping the
            // schema + adding the corresponding .sqm to refresh it. Run
            // `./gradlew :shared:verifyCommonMainDatabaseMigration` manually before
            // shipping a schema change to verify migrations against the snapshot.
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
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
