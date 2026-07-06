import org.apache.tools.ant.filters.ReplaceTokens
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("solarcalc.common-conventions")
}

tasks.named<BootJar>("bootJar") {
    enabled = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    layered {
        enabled.set(true)
    }
}

tasks.named<Jar>("jar") {
    enabled = false
}

dependencies {
    implementation(project(":solarcalc-domain-models"))
    implementation(project(":solarcalc-domain-services"))
    implementation(project(":solarcalc-rest-api"))

    implementation(libs.spring.boot.starter.log4j2)
    implementation(libs.logging.log4j.slf4j.impl)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.aspectjweaver)

    annotationProcessor(platform(libs.bom.spring.boot))
    annotationProcessor(libs.spring.boot.configuration.processor)
    developmentOnly(platform(libs.bom.spring.boot))
    developmentOnly(libs.spring.boot.devtools)

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.liquibase)

    implementation(libs.database.querydsl.jpa.spring)
    implementation(libs.database.tomcat.jdbc)
    implementation(libs.database.mariadb.java.client)

    implementation(libs.bundles.micrometer.tracing)
    implementation(libs.micrometer.registry.prometheus)

    kover(project(":solarcalc-domain-services"))

    testImplementation(libs.testing.testcontainers.mariadb)
    testImplementation(libs.testing.testcontainers.junit.jupiter)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.resttestclient)
    testImplementation(libs.spring.boot.starter.restclient.test)
    testImplementation(libs.testing.cucumber.gherkin.lib)
    testImplementation(libs.testing.junit.platform.suite)
    testRuntimeOnly(libs.testing.cucumber.junit.platform)

    runtimeOnly(libs.database.h2)
}

// replace version number in resource files
tasks.processResources {
    filesMatching("**/banner.txt") {
        filter(mapOf("tokens" to mapOf("server.version" to version.toString())), ReplaceTokens::class.java)
    }
}

// from task_dependencies.gradle
tasks.named("resolveMainClassName") {
    mustRunAfter(":generateLicenseReport")
}
