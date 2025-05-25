plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)

    id("maven-publish")
}

group = "minerslab.mcd.api"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net")
}

dependencies {
    testImplementation(kotlin("test"))

    api(project(":mcd-core"))
    api(libs.adventure.brigadier)
    api(libs.adventure.nbt)
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