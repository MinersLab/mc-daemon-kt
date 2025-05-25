plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "mcd"

include(
    "mcd-core",
    "mcd-api", "mcd-permission-api",
    "mcd-launcher",
    "mcd-example-plugin"
)
