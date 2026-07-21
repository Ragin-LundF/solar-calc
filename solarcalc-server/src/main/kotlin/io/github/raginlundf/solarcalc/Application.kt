package io.github.raginlundf.solarcalc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportRuntimeHints

@SpringBootApplication
@ImportRuntimeHints(LiquibaseNativeHints::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
