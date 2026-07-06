plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

// Convert a version-catalog plugin alias into its marker artifact so that
// precompiled convention plugins can apply it via id("...").
fun DependencyHandler.plugin(dep: Provider<PluginDependency>) =
    dep.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }

dependencies {
    // Expose the generated type-safe `libs` accessors to convention plugins.
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(plugin(libs.plugins.kotlin.jvm))
    implementation(plugin(libs.plugins.kotlin.jpa))
    implementation(plugin(libs.plugins.kotlin.spring))
    implementation(plugin(libs.plugins.spring.boot))
    implementation(plugin(libs.plugins.ksp))
    implementation(plugin(libs.plugins.liquibase.gradle))
    implementation(plugin(libs.plugins.owasp.depcheck))
    implementation(plugin(libs.plugins.version.catalog.update))
    implementation(plugin(libs.plugins.license.report))
    implementation(plugin(libs.plugins.detekt))
    implementation(plugin(libs.plugins.kover))

    // the liquibase gradle plugin needs liquibase-core (liquibase.Scope) on the
    implementation(libs.buildscript.liquibase.core)
}
