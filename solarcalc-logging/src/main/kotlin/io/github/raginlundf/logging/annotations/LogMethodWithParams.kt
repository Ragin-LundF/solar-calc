package io.github.raginlundf.logging.annotations


/**
 * This annotation is used to mark a method for logging with parameters.
 *
 * @property logInput Switch to activate or deactivate input parameter logging.
 * @property logOutput Switch to activate or deactivate output parameter logging.
 * @property obfuscateParameters Array of parameter names to obfuscate at input and output logging.
 * @property obfuscateParametersInput Array of parameter names to obfuscate at input logging.
 * @property obfuscateParametersOutput Array of parameter names to obfuscate at output logging.
 * @property skipParameters Array of parameter names to skip for input and output logging.
 * @property skipParametersInput Array of parameter names to skip for input logging.
 * @property skipParametersOutput Array of parameter names to skip for output logging.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Suppress("LongParameterList")
annotation class LogMethodWithParams (
    // Switch to activate/deactivate input parameter logging.
    val logInput: Boolean = true,
    // Switch to activate/deactivate output parameter logging.
    val logOutput: Boolean = true,

    // Obfuscate parameters at input and output logging
    val obfuscateParameters: Array<String> = [],
    // Obfuscate parameters at input logging
    val obfuscateParametersInput: Array<String> = [],
    // Obfuscate parameters at output logging
    val obfuscateParametersOutput: Array<String> = [],

    // Parameters, which should be skipped for input and output logging.
    val skipParameters: Array<String> = [],
    // Parameters, which should be skipped for input logging.
    val skipParametersInput: Array<String> = [],
    // Parameters, which should be skipped for output logging.
    val skipParametersOutput: Array<String> = []
)
