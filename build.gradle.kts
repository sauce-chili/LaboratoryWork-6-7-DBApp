import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val kotlin_version: String by project
val postgres_version: String by project
val h2_version: String by project
val serialization_version: String by project
val coil_version: String by project

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization") version "1.9.22"
}

group = "ru.vstu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)


    // db (postgres)
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("com.h2database:h2:$h2_version")

    // ui
    implementation("org.jetbrains.compose.material3:material3-desktop:1.6.0")

    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")

    // tests
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ModularWorkDBApp"
            packageVersion = "1.0.0"
        }
    }
}
