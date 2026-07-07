pluginManagement {
    repositories {
        // set the flag in the gradle.properties
        gradlePluginPortal()
        mavenCentral()
        if (providers.gradleProperty("useMavenLocal").map(String::toBoolean).getOrElse(false)) {
            mavenLocal()
        }
    }
}

dependencyResolutionManagement {
    repositories {
        // set the flag in the gradle.properties
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        if (providers.gradleProperty("useMavenLocal").map(String::toBoolean).getOrElse(false)) {
            mavenLocal()
        }
    }
}

rootProject.name = "solar-calc"

include("solarcalc-domain-models")
include("solarcalc-domain-services")
include("solarcalc-dtos")
include("solarcalc-kotlin-extensions")
include("solarcalc-logging")
include("solarcalc-rest-api")
include("solarcalc-server")
