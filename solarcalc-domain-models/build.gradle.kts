plugins {
    id("solarcalc.common-conventions")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(project(":solarcalc-kotlin-extensions"))

    implementation(libs.database.hibernate.core)
    implementation(libs.spring.context)
    implementation(libs.spring.data.jpa)

    implementation(libs.database.liquibase.core)

    implementation(libs.database.querydsl.jpa.spring)
    implementation(libs.database.querydsl.kotlin)
    ksp(libs.database.querydsl.ksp.codegen)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.data.jpa)
    testImplementation(libs.testing.testcontainers.mariadb)
    testImplementation(libs.testing.testcontainers.junit.jupiter)
    testRuntimeOnly(libs.database.mariadb.java.client)
    testRuntimeOnly(libs.database.tomcat.jdbc)
}
