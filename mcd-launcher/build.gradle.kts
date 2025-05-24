import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.shadow)

    id("maven-publish")
    application
}


application {
    mainClass = "minerslab.mcd.McDaemonLauncherKt"
}

group = "minerslab.mcd.launcher"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net")
}

dependencies {
    testImplementation(kotlin("test"))

    api(project(":mcd-api"))
    api(project(":mcd-api-permission"))
    api(project(":mcd-core"))
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

tasks.withType<ShadowJar> {
    mergeServiceFiles()
}
