import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    idea
    `java-library`
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("project-report")
    id("org.springframework.boot")
}

val libs = the<LibrariesForLibs>()

tasks.register<CreateStartScripts>("createStartScripts") {
    description = "start scripts for the application"
    mainClass.set("io.github.raginlundf.solarcalc.Application")
    applicationName = rootProject.name
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
    archiveClassifier.set("") // remove "plain" suffix for jar files
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
    compilerOptions {
        // jvmTarget is driven by the toolchain above; Kotlin 2.4 caps at JVM 24
        // and falls back gracefully, so we do not pin it to javaVersion here.
        freeCompilerArgs.add("-Xjvm-default=all")
        // Emit method parameter names so the logging aspect's signature.parameterNames
        // resolves in native images (which strip the LocalVariableTable).
        javaParameters.set(true)
    }
}

tasks.named<JavaCompile>("compileJava") {
    options.encoding = "UTF-8"
    options.isIncremental = true
}
tasks.named<JavaCompile>("compileTestJava") {
    options.encoding = "UTF-8"
    options.isIncremental = true
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/kotlin"))
        }
    }
}

// ── from gradle_excludes.gradle ────────────────────────────────────────────────
configurations.configureEach {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    exclude(group = "ch.qos.logback", module = "logback-classic")
    exclude(group = "ch.qos.logback", module = "logback-core")
    exclude(group = "com.zaxxer", module = "HikariCP")
}

// ── from dependencies.gradle ───────────────────────────────────────────────────
dependencies {
    /*
    constraints {
        api(libs.bundles.constraints)
    }
    */

    implementation(platform(libs.bom.spring.boot))
    implementation(platform(libs.bom.log4j))
    implementation(platform(libs.bom.micrometer))
    implementation(platform(libs.bom.querydsl))
    implementation(platform(libs.bom.jackson))
    testImplementation(platform(libs.bom.testcontainers))
    testImplementation(platform(libs.bom.junit))

    implementation(libs.logging.kotlin.logging)

    testRuntimeOnly(libs.testing.junit.platform.launcher)
    testImplementation(libs.testing.junit.jupiter)
    testImplementation(libs.testing.kotlin.test.junit5)
    testImplementation(libs.testing.springmockk)
}
