plugins {
    id("solarcalc.common-conventions")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":solarcalc-domain-models"))
    implementation(project(":solarcalc-dtos"))
    implementation(project(":solarcalc-kotlin-extensions"))

    implementation(libs.jakarta.validation.api)
    implementation(libs.spring.context)
    implementation(libs.spring.security.core)
    implementation(libs.spring.security.oauth2.jose)
    implementation(libs.spring.webmvc)
    implementation(libs.spring.data.jpa)

    implementation(libs.database.querydsl.jpa.spring)
    implementation(libs.database.querydsl.kotlin)

    implementation(libs.spring.context.support)

    implementation(libs.bundles.konvert.api)
    ksp(libs.konvert)
    ksp(libs.konvert.spring.injector)

    testImplementation(libs.spring.test)
}
