import com.palantir.gradle.gitversion.VersionDetails
import java.util.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.git.version)

    id("maven-publish")
}

group = "minerslab.mcd.core"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.ktor:ktor-server-core:3.2.2")
    implementation("io.ktor:ktor-server-core:3.2.2")
    testImplementation(kotlin("test"))

    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.serialization.hocon)
    api(libs.kotlinx.coroutines)

    api(libs.adventure.parser)
    api(libs.adventure.registry)
    api(libs.adventure.event)
    api(libs.minecraft.rcon)

    api(libs.ktor.server.netty)
    api(libs.ktor.server.core)
    api(libs.ktor.server.websockets)

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

val versionDetails: groovy.lang.Closure<VersionDetails> by extra

tasks.processResources {
    val resourceTargets = listOf("META-INF/mcd-core.properties")
    val replaceProperties = mapOf(
        "gradle" to gradle,
        "rootProject" to rootProject,
        "project" to project,
        "versionDetails" to versionDetails(),
        "date" to Date(),
    )
    filesMatching(resourceTargets) {
        expand(replaceProperties)
    }
}
