plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "minerslab.mcd"
version = "1.0.0"

subprojects {
    version = rootProject.version
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
