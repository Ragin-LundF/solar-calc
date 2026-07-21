package io.github.raginlundf.solarcalc.jackson

import org.springframework.aot.hint.BindingReflectionHintsRegistrar
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.TypeFilter

/**
 * Jackson reads/writes the DTOs reflectively (constructors, getters, fields); GraalVM needs that
 * surface registered or response bodies serialize to "{}". BindingReflectionHintsRegistrar registers
 * it recursively, so field types in other packages (the enums in domain-models, nested DTOs) are
 * covered by registering the top-level DTOs. Scan the dtos package instead of listing 21 classes so
 * new DTOs are picked up automatically.
 */
class JacksonBindingNativeHints : RuntimeHintsRegistrar {

    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        val cl = classLoader ?: javaClass.classLoader
        val binding = BindingReflectionHintsRegistrar()
        val scanner = ClassPathScanningCandidateComponentProvider(false).apply {
            addIncludeFilter(TypeFilter { _, _ -> true })
        }

        scanner.findCandidateComponents(DTO_PACKAGE).forEach { bean ->
            val className = bean.beanClassName ?: return@forEach
            val type = runCatching { Class.forName(className, false, cl) }.getOrNull() ?: return@forEach
            binding.registerReflectionHints(hints.reflection(), type)
        }
    }

    private companion object {
        const val DTO_PACKAGE = "io.github.raginlundf.solarcalc.dtos"
    }
}
