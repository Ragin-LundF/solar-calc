plugins {
    id("solarcalc.common-conventions")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":solarcalc-domain-models"))
    implementation(project(":solarcalc-kotlin-extensions"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jakarta.validation.api)
}
