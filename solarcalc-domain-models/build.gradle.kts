plugins {
    id("solarcalc.common-conventions")
    id("org.jetbrains.kotlin.plugin.jpa")
    id("com.google.devtools.ksp")
    alias(libs.plugins.hibernate.orm)
}

// Build-time bytecode enhancement of the JPA entities. Required for the GraalVM native image:
// native runs with hibernate.bytecode.provider=none (no runtime ByteBuddy), so lazy @ManyToOne
// associations can't get a runtime HibernateProxy. Enhancement wires lazy loading at build time
// instead. `languages` must include "kotlin" — the entities are Kotlin, and the plugin defaults to
// enhancing only the java compile output.
hibernate {
    languages = setOf("java", "kotlin")
    enhancement {
        enableLazyInitialization = true
        enableDirtyTracking = true
    }
}

dependencies {
    implementation(project(":solarcalc-kotlin-extensions"))

    implementation(libs.database.hibernate.core)
    implementation(libs.spring.context)
    api(libs.spring.data.jpa)

    implementation(libs.database.liquibase.core)

    api(libs.kotlin.reflect)

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
