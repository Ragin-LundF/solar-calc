import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.testing.base.TestingExtension

val jvmArguments = listOf(
    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
    "--add-opens", "java.base/java.util=ALL-UNNAMED",
    "--add-opens", "java.base/java.time=ALL-UNNAMED",
    "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100",
    "-XX:+ParallelRefProcEnabled",
    "-XX:+UseCompressedOops",
    "-XX:+AlwaysPreTouch", "-XX:+TieredCompilation", "-XX:+UseStringDeduplication",
)

configurations {
    create("cucumberRuntime") {
        extendsFrom(configurations["testImplementation"])
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    exclude("**/*Cucumber*")
    include("**/*Test*")

    minHeapSize = "512m"
    maxHeapSize = "3072m"
    jvmArgs = jvmArguments
    onlyIf("Execute only if test task is called directly") {
        gradle.startParameter.taskNames.toString().contains("test")
    }
}

if (project.name == "solarcalc-server") {
    val testSuite = extensions.getByType<TestingExtension>()
        .suites.named("test", JvmTestSuite::class.java)

    tasks.register<Test>("cucumber") {
        useJUnitPlatform()
        group = "verification"
        dependsOn("assemble")
        exclude("**/*Test*")
        include("**/*CucumberRunner*")
        systemProperty("cucumber.junit-platform.naming-strategy", "long")
        systemProperty("spring.profiles.active", "cucumber")
        testClassesDirs = testSuite.get().sources.output.classesDirs
        classpath = testSuite.get().sources.runtimeClasspath
        jvmArgs = jvmArguments
        onlyIf("Execute only if cucumber task is called directly") {
            gradle.startParameter.taskNames.contains("cucumber")
        }
    }
}
