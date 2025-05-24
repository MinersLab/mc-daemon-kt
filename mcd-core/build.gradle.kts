plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)

    id("maven-publish")
}

group = "minerslab.mcd.core"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))

    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.serialization.hocon)
    api(libs.kotlinx.coroutines)

    api(libs.adventure.parser)

    api(libs.ktor.server.netty)
    api(libs.ktor.server.core)

    api(libs.logback.classic)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

configure<PublishingExtension> {
    publications.create<MavenPublication>("maven") {
        from(components.getByName("kotlin"))
    }
}
