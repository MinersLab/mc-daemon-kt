plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)

    id("maven-publish")
}

group = "minerslab.mcd.example.plugin"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net")
}

dependencies {
    testImplementation(kotlin("test"))

    api(project(":mcd-core"))
    api(project(":mcd-api"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks {
    val copyJarToPlugins by registering(Copy::class) {
        val dir = project(":mcd-launcher").projectDir.resolve(".mcd/plugins/build")
        dir.deleteRecursively()
        from(jar.get().archiveFile)
        into(dir)
        rename { it }
    }

    jar {
        finalizedBy(copyJarToPlugins)
    }
}
