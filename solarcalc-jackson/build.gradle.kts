plugins {
    id("solarcalc.common-conventions")
}

dependencies {
    implementation(platform(libs.bom.jackson))
    api(libs.bundles.jackson)
    implementation(libs.spring.context)
}
