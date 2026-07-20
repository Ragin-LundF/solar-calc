import org.apache.tools.ant.filters.ReplaceTokens
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("solarcalc.common-conventions")
    // Version-less: the plugin is provided via buildSrc's classpath (see buildSrc/build.gradle.kts).
    id("org.graalvm.buildtools.native")
}

tasks.named<BootJar>("bootJar") {
    enabled = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    layered {
        enabled = true
    }
}

// Native executable via `./gradlew :solarcalc-server:nativeCompile` ->
// build/native/nativeCompile/solar-calc. Requires GraalVM (native-image) for JDK 25 on
// the build machine; the binary is OS/arch-specific, so build it on Linux to deploy on Linux.
graalvmNative {
    binaries.named("main") {
        imageName.set("solar-calc")
        // This module doesn't apply the `application` plugin, so the GraalVM plugin has no
        // main class at init and defaults the binary to a shared library. Force an executable
        // and set the entry point explicitly.
        sharedLibrary.set(false)
        mainClass.set("io.github.raginlundf.solarcalc.ApplicationKt")
        // The plain `jar` is disabled here, so Boot's AOT classpath drops the app's own
        // classes and ApplicationKt is only reachable inside the bootJar (BOOT-INF/classes),
        // where native-image can't see it. Add the plain main output back to the classpath.
        classpath(sourceSets["main"].output)
    }
}

// Cross-build the Linux native binary in Docker — no local GraalVM needed (see
// devops/Dockerfile.native). Output: build/native/docker/solar-calc-<platform>.
fun Exec.configureNativeCompileDocker(platform: String?) {
    group = "build"
    workingDir = rootProject.projectDir
    // When no --platform is given, Docker builds for the host; label it from the host arch.
    val hostArch = when (System.getProperty("os.arch")) {
        "aarch64", "arm64" -> "arm64"
        "x86_64", "amd64" -> "amd64"
        else -> System.getProperty("os.arch")
    }
    val binName = "solar-calc-" + (platform ?: "linux/$hostArch").replace('/', '-')
    // When Gradle runs from the IDE, macOS gives it a stripped PATH without /usr/local/bin
    // (where the docker CLI lives). Gradle resolves the executable against its own PATH, not
    // the task's, so it fails with "Could not start 'docker'". Launch /usr/bin/env (always at a
    // fixed path) with an augmented PATH; env then finds docker regardless of how Gradle started.
    environment("PATH", "/usr/local/bin:/opt/homebrew/bin:" + (System.getenv("PATH") ?: ""))
    val outDir = layout.buildDirectory.dir("native/docker")
    commandLine(
        buildList {
            addAll(listOf("/usr/bin/env", "docker", "build", "-f", "devops/Dockerfile.native", "--target", "export"))
            addAll(listOf("--build-arg", "BIN_NAME=$binName"))
            platform?.let { addAll(listOf("--platform", it)) }
            addAll(listOf("--output", "type=local,dest=${outDir.get().asFile}", "."))
        }
    )
    doLast { logger.lifecycle("Native binary: ${outDir.get().file(binName).asFile}") }
}

// Native binary for the host architecture. Override arch with -Pnative.platform=linux/amd64.
tasks.register<Exec>("nativeCompileDocker") {
    description = "Compile the Linux native binary inside Docker (no local GraalVM required)."
    configureNativeCompileDocker(providers.gradleProperty("native.platform").orNull)
}

// Native binary pinned to linux/amd64 (x86_64 servers). Slow under emulation on Apple Silicon.
tasks.register<Exec>("nativeCompileDockerLinuxAmd64") {
    description = "Compile the linux/amd64 native binary inside Docker (no local GraalVM required)."
    configureNativeCompileDocker("linux/amd64")
}

tasks.named<Jar>("jar") {
    enabled = false
}

dependencies {
    implementation(project(":solarcalc-domain-models"))
    implementation(project(":solarcalc-domain-services"))
    implementation(project(":solarcalc-rest-api"))
    implementation(project(":solarcalc-jackson"))

    implementation(libs.spring.aop)
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
