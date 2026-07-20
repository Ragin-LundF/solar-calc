plugins {
    id("solarcalc.common-conventions")
}

dependencies {
    implementation(project(":solarcalc-domain-models"))
    implementation(project(":solarcalc-kotlin-extensions"))
    implementation(libs.jakarta.validation.api)
}
