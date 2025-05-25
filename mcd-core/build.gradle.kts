import com.palantir.gradle.gitversion.VersionDetails
import java.util.Date

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
    testImplementation(kotlin("test"))

    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.serialization.hocon)
    api(libs.kotlinx.coroutines)

    api(libs.adventure.parser)
    api(libs.minecraft.rcon)

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
