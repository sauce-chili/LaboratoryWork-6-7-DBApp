val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val h2_version: String by project
val kodein_version = "7.9.0"
val kotlinx_date_version = "0.6.0"

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.11"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

group = "ru.vstu"
version = "0.0.1"

application {
    mainClass.set("ru.vstu.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // ktor
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinx_date_version")

    // coroutines


    // DI (KoDeIn)
    implementation("org.kodein.di:kodein-di:${kodein_version}")
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:${kodein_version}")

    // db (postgres)
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("com.h2database:h2:$h2_version")

    // logs
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // tests
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
