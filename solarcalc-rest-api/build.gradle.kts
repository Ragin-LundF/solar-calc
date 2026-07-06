plugins {
    id("solarcalc.common-conventions")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":solarcalc-domain-services"))
    implementation(project(":solarcalc-dtos"))

    implementation(libs.spring.web)
    implementation(libs.spring.webmvc)
    implementation(libs.spring.security.core)
    implementation(libs.spring.security.web)
    implementation(libs.spring.security.config)
    implementation(libs.spring.security.oauth2.jose)
    implementation(libs.spring.security.oauth2.resource.server)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.jakarta.servlet.api)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logging.slf4j.api)

    testImplementation(libs.spring.test)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.testing.springmockk)
}


