plugins {
    id("solarcalc.common-conventions")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.aspectjweaver)
    implementation(libs.spring.context)
    implementation(libs.kotlinx.serialization.json)
}
