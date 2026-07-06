plugins {
    id("org.owasp.dependencycheck")
}

dependencyCheck {
    formats = listOf("XML", "HTML")

    failBuildOnCVSS = 1f
    suppressionFile = "${rootProject.projectDir}/config/dependency-check-suppressions.xml"
    // Only check the compile and runtime classpath and ignore findings in other configurations
    scanConfigurations = listOf(
        "compileClasspath",
        "productionCompileClasspath",
        "runtimeClasspath",
        "productionRuntimeClasspath",
    )
    analyzers {
        assemblyEnabled = false
        nugetconfEnabled = false
        nuspecEnabled = false
        nodeEnabled = false
    }
}
