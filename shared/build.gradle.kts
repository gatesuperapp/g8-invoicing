plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight") version "2.2.1"
    kotlin("plugin.serialization") version "2.3.21"
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
        // kotlin.time.Clock / Instant / Duration are still marked @ExperimentalTime
        // in Kotlin 2.2.x's stdlib. Since kotlin-stdlib 2.3.x is pulled in transitively
        // by a few libraries (Koin 4.2.x, Compose Multiplatform 1.8, kotlinx-coroutines
        // 1.11), `kotlinx.datetime.Clock` gets shadowed by the stdlib one. Opt-in
        // globally rather than sprinkle @file:OptIn on every touched file.
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }

        // Create jvmMain source set shared between Android and Desktop
        val jvmMain by creating {
            dependsOn(commonMain.get())
        }

        commonMain.dependencies {
            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

            // BigDecimal KMP - use api() to export to dependent modules
            api("com.ionspin.kotlin:bignum:0.3.10")

            // Multiplatform Settings (for language persistence + token storage)
            // api() so the app module can build SharedPreferencesSettings(EncryptedSharedPreferences)
            api("com.russhwolf:multiplatform-settings-no-arg:1.3.0")

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
            implementation("sh.calvin.reorderable:reorderable:3.1.0")

            // Navigation Compose Multiplatform (JetBrains)
            api("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha13")

            // Lifecycle ViewModel Compose Multiplatform (JetBrains)
            api("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
            // Lifecycle Runtime Compose — exposes LifecycleEventEffect / LocalLifecycleOwner
            // in commonMain (needed for ON_RESUME auto-refresh in the Account screen).
            api("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

            // Koin for KMP
            api("io.insert-koin:koin-core:4.2.2")
            api("io.insert-koin:koin-compose:4.2.2")
            api("io.insert-koin:koin-compose-viewmodel:4.2.2")

            // Ktor Client (HTTP) — api() so the app module can configure HttpClient
            api("io.ktor:ktor-client-core:3.5.1")
            api("io.ktor:ktor-client-content-negotiation:3.5.1")
            api("io.ktor:ktor-serialization-kotlinx-json:3.5.1")
            api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

            // SQLDelight - use api() to export to app module
            api("app.cash.sqldelight:coroutines-extensions:2.2.1")
        }

        jvmMain.dependencies {
            // iText for PDF generation (shared between Android and Desktop)
            implementation("com.itextpdf:itext7-core:9.5.0")
        }

        androidMain {
            dependsOn(jvmMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
                // SQLDelight Android Driver
                implementation("app.cash.sqldelight:android-driver:2.2.1")
                // AppCompat for locale management
                implementation("androidx.appcompat:appcompat:1.7.1")
                // Ktor engine Android — api() so the app module can use OkHttp engine
                api("io.ktor:ktor-client-okhttp:3.5.1")
                // Encrypted storage for tokens
                implementation("androidx.security:security-crypto:1.1.0")
                // Material3 pinned above what CMP 1.8.2 bundles, to pull in the fix
                // for ModalBottomSheet + IME anchor wobble (Google b/289824811,
                // commit Ied801). Without this override the sheet re-runs its
                // anchor animation every time WindowInsets.ime changes, which
                // races the OS keyboard animation and produces a visible jump
                // when a TextField inside the sheet receives focus.
                // TODO: remove this line once CMP bundles Material3 >= 1.5.0
                // stable — the multiplatform artifact will then include the fix.
                implementation("androidx.compose.material3:material3-android:1.5.0-alpha19")
            }
        }

        iosMain.dependencies {
            // SQLDelight Native Driver for iOS
            implementation("app.cash.sqldelight:native-driver:2.2.1")
            // Ktor engine iOS
            implementation("io.ktor:ktor-client-darwin:3.5.1")
        }

        val desktopMain by getting {
            dependsOn(jvmMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.11.0")
                // SQLDelight JVM Driver for Desktop
                implementation("app.cash.sqldelight:sqlite-driver:2.2.1")
                // Ktor engine Desktop
                implementation("io.ktor:ktor-client-cio:3.5.1")
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
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
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
