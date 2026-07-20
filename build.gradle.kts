plugins {
    base
    id("solarcalc.kover-aggregation-conventions")
    id("solarcalc.license-report-conventions")
    id("solarcalc.version-catalog-conventions")
    id("org.liquibase.gradle")
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "9.6.1"
}
