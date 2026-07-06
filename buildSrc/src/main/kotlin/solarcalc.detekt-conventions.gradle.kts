import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import dev.detekt.gradle.plugin.getSupportedKotlinVersion

plugins {
    id("dev.detekt")
}

detekt {
    config.setFrom("${rootDir}/config/detekt.yml")
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    exclude("**/gen/**")
    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        checkstyle.required.set(true) // checkstyle-like xml format for integrations like Jenkins (was `xml` pre-2.0)
        sarif.required.set(true)
        // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
    }
    jvmTarget = JavaVersion.toVersion(javaVersion).toString()
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = JavaVersion.toVersion(javaVersion).toString()
}

tasks.named("assemble") {
    finalizedBy(tasks.named("detekt"))
}

// detekt must resolve its kotlin dependencies to the version it was built against
configurations.matching { it.name == "detekt" }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(getSupportedKotlinVersion())
        }
    }
}
