package io.github.raginlundf.solarcalc

import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

/**
 * Liquibase discovers its implementations (changes, datatypes, SQL generators, databases, …) via
 * ServiceLoader and then reads/writes their bean properties reflectively — e.g. it calls
 * AddUniqueConstraintChange.getDeferrable() while parsing the changelog. GraalVM needs every one of
 * those classes registered for reflection. Rather than chase MissingReflectionRegistrationError one
 * getter at a time, register the full set by reading Liquibase's own service-loader descriptors
 * under META-INF/services, so this stays correct across Liquibase versions.
 *
 * Resources (changelog XMLs, ResultSet proxy) are registered separately in
 * solarcalc-domain-models' reachability-metadata.json.
 */
class LiquibaseNativeHints : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        val cl = classLoader ?: javaClass.classLoader
        val reflection = hints.reflection()

        // Every implementation Liquibase loads via ServiceLoader.
        PathMatchingResourcePatternResolver(cl)
            .getResources("classpath*:META-INF/services/liquibase.*")
            .forEach { resource ->
                resource.inputStream.bufferedReader().useLines { lines ->
                    lines
                        .map { it.substringBefore('#').trim() }
                        .filter { it.isNotEmpty() }
                        .forEach { className ->
                            reflection.registerType(TypeReference.of(className), *MEMBERS)
                        }
                }
            }

        // Nested config/visitor types set reflectively but not listed in any service file.
        EXTRA_TYPES.forEach { reflection.registerType(TypeReference.of(it), *MEMBERS) }

        hints.resources().apply {
            registerPattern("liquibase/i18n/liquibase-core*.properties")
            registerPattern("liquibase.build.properties")
            registerPattern("www.liquibase.org/xml/ns/dbchangelog/*.xsd")
            registerResourceBundle("liquibase.i18n.liquibase-core")
        }
    }

    private companion object {
        val MEMBERS = arrayOf(
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
        )

        val EXTRA_TYPES = listOf(
            "liquibase.change.ColumnConfig",
            "liquibase.change.AddColumnConfig",
            "liquibase.change.ConstraintsConfig",
            "liquibase.change.core.LoadDataColumnConfig",
            "liquibase.sql.visitor.PrependSqlVisitor",
            "liquibase.sql.visitor.AppendSqlVisitor",
            "liquibase.sql.visitor.ReplaceSqlVisitor",
            "liquibase.sql.visitor.RegExpReplaceSqlVisitor",
        )
    }
}
