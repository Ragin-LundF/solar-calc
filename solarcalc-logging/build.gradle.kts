plugins {
    id("solarcalc.common-conventions")
}

dependencies {
    implementation(project(":solarcalc-jackson"))
    implementation(libs.aspectjweaver)
    implementation(libs.spring.aop)
    implementation(libs.spring.context)
}
