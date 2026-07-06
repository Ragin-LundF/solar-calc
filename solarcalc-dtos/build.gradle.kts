plugins {
    id("solarcalc.common-conventions")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":solarcalc-domain-models"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jakarta.validation.api)
}
