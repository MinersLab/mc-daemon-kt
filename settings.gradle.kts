plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "mcd"

include(
    "mcd-core",
    "mcd-api", "mcd-api-permission",
    "mcd-launcher",
    "mcd-example-plugin"
)
