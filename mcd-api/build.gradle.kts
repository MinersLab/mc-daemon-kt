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
    api(libs.adventure.api)
    api(libs.adventure.registry)
    api(libs.adventure.extra.kotlin)
    api(libs.adventure.text.plain)
    api(libs.adventure.text.ansi)
    api(libs.adventure.text.legacy)
    api(libs.adventure.text.minimessage)
    api(libs.adventure.text.json)
    runtimeOnly(libs.adventure.text.json.gson)
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
