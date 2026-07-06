import org.gradle.api.Project

/** Typed accessors for the untyped gradle.properties values used by the build. */
val Project.javaVersion: String
    get() = property("javaVersion").toString()

val Project.codegenProcessedSwaggerFiles: String
    get() = property("codegenProcessedSwaggerFiles").toString()

val Project.codegenProcessedSwaggerResources: String?
    get() = findProperty("codegenProcessedSwaggerResources")?.toString()

/** Import mappings shared by all modules that run the openapi code generator. */
val codegenImportMappings = listOf(
    "Page",
    "ErrorMessage",
    "ErrorResponse",
)

/**
 * Task-name fragments derived from the configured OpenAPI files, e.g. "solarcalcadmin".
 * Mirrors the capitalisation logic previously in the root build.gradle.
 */
val Project.apiTaskNames: List<String>
    get() {
        val marker = "openapi-"
        val files = buildList {
            addAll(codegenProcessedSwaggerFiles.split(","))
            codegenProcessedSwaggerResources?.let { addAll(it.split(",")) }
        }.map(String::trim).filter(String::isNotEmpty)

        return files.map { file ->
            file.substring(file.indexOf(marker) + marker.length)
                .replace(".yaml", "")
                .replaceFirstChar(Char::uppercaseChar)
        }
    }
