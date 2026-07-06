package io.github.raginlundf.logging.annotations

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * Annotation that can be applied to methods to indicate that the duration of the method execution
 * should be logged.
 *
 * The LogDuration annotation can be used to annotate methods and allows the ability to measure the
 * time taken to execute the method.
 *
 * Usage:
 *
 * @LogDuration
 * fun methodToBeMeasured() {
 *    */
@Target(FUNCTION)
@Retention(RUNTIME)
annotation class LogDuration
