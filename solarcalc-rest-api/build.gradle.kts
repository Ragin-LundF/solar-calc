plugins {
    id("solarcalc.common-conventions")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":solarcalc-domain-services"))
    implementation(project(":solarcalc-domain-models"))

    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation(libs.spring.security.core)
    implementation(libs.spring.security.web)
    implementation(libs.spring.security.config)
    implementation(libs.spring.security.oauth2.jose)
    implementation(libs.spring.security.oauth2.resource.server)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.jakarta.servlet.api)

    implementation(libs.bundles.konvert.api)
    ksp(libs.konvert)
    ksp(libs.konvert.spring.injector)

    testImplementation(libs.spring.test)
    testImplementation(libs.spring.boot.starter.test)
}
