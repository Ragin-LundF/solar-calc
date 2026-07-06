plugins {
    id("org.jetbrains.kotlinx.kover")
}

// Register this subproject with the root aggregated kover report.
rootProject.dependencies.add("kover", project)
