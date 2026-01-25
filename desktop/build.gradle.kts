import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // Koin for DI
    implementation("io.insert-koin:koin-core:4.0.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
}

compose.desktop {
    application {
        mainClass = "com.a4a.g8invoicing.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "G8 Invoicing"
            packageVersion = "1.4.0"
            description = "G8 Invoicing - Invoice Management Application"
            vendor = "A4A"

            macOS {
                bundleID = "com.a4a.g8invoicing"
                iconFile.set(project.file("icons/icon.icns"))
            }

            windows {
                iconFile.set(project.file("icons/icon.ico"))
                menuGroup = "G8 Invoicing"
            }

            linux {
                iconFile.set(project.file("icons/icon.png"))
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
