plugins {
    id("solarcalc.common-conventions")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":solarcalc-kotlin-extensions"))
    api(libs.kotlinx.serialization.json)

    implementation(libs.database.hibernate.core)
    implementation(libs.spring.context)
    api(libs.spring.data.jpa)

    implementation(libs.database.liquibase.core)

    api(libs.database.querydsl.jpa.spring)
    api(libs.database.querydsl.kotlin)
    ksp(libs.database.querydsl.ksp.codegen)

    testImplementation(libs.spring.test)
    testImplementation(libs.testing.junit.jupiter)
    testImplementation(libs.testing.kotlin.test.junit5)
    testImplementation(libs.testing.testcontainers.mariadb)
    testImplementation(libs.testing.testcontainers.junit.jupiter)
    testRuntimeOnly(libs.testing.junit.platform.launcher)
    testRuntimeOnly(libs.database.mariadb.java.client)
    testRuntimeOnly(libs.database.tomcat.jdbc)
}
