plugins {
    id("solarcalc.common-conventions")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":solarcalc-domain-services"))

    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation(libs.spring.security.core)
    implementation(libs.spring.security.web)
    implementation(libs.spring.security.config)
    implementation(libs.spring.boot.starter.validation)

    implementation(libs.bundles.konvert.api)
    ksp(libs.konvert)
    ksp(libs.konvert.spring.injector)

    testImplementation(libs.spring.test)
    testImplementation(libs.spring.boot.starter.test)
}
