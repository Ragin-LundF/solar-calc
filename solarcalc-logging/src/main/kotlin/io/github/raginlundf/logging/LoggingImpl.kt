package io.github.raginlundf.logging

import io.github.raginlundf.logging.annotations.LogMethodWithParams
import io.github.raginlundf.logging.utils.ObfuscationUtil.OBFUSCATION
import io.github.raginlundf.logging.utils.ObfuscationUtil.obfuscate
import io.github.raginlundf.logging.utils.ObfuscationUtil.skip
import io.github.raginlundf.logging.utils.ResponseMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.time.measureTimedValue

@Aspect
@Component
class LoggingImpl {
    /**
     * Logs the execution time of a method.
     *
     * @param joinPoint The join point representing the method being executed.
     * @return The result of the method execution.
     * @throws Throwable If an error occurs during the method execution.
     */
    @Around("@annotation(io.github.raginlundf.logging.annotations.LogDuration)")
    @Throws(Throwable::class)
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val measureTimed = measureTimedValue {
            joinPoint.proceed()
        }
        val methodSignature = joinPoint.signature.toShortString()
        log.info { "[Logging] $methodSignature execution took ${measureTimed.duration}" }
        return measureTimed.value
    }

    /**
     * Logs the input parameters of a method if the specified annotation is present.
     *
     * @param joinPoint The join point representing the method being executed.
     * @param logMethodWithParams The annotation used to mark the method for parameter logging.
     */
    @Before("@annotation(logMethodWithParams)")
    fun logMethodParams(joinPoint: JoinPoint, logMethodWithParams: LogMethodWithParams) {
        if (logMethodWithParams.logInput) {
            val signature = joinPoint.signature as MethodSignature
            val paramNames = signature.parameterNames
            val paramTypes = signature.parameterTypes
            val args: Array<Any?> = joinPoint.args
            val skipIn = skipParamInput(logMethodWithParams)
            val obfuscateIn = obfuscateParamInput(logMethodWithParams)

            val logMsg = IntStream.range(0, args.size) // skip root parameters
                .filter { i: Int -> !skipIn.contains(paramNames[i]) }
                .mapToObj { i: Int -> prepareParamLogMessage(i, paramTypes, paramNames, args, skipIn, obfuscateIn) }
                .collect(
                    Collectors.joining(
                        "",
                        "[Logging] Input parameter for ${signature.toShortString()}:",
                        ""
                    )
                )
            log.info { logMsg }
        }
    }

    /**
     * Logs the result of a method execution.
     *
     * @param joinPoint The join point representing the method being executed.
     * @param logMethodWithParams The annotation used to mark the method for parameter logging.
     * @param result The result of the method execution.
     */
    @AfterReturning(pointcut = "@annotation(logMethodWithParams)", returning = "result")
    fun logMethodResult(
        joinPoint: JoinPoint,
        logMethodWithParams: LogMethodWithParams,
        result: Any?
    ) {
        if (logMethodWithParams.logOutput) {
            val signature = joinPoint.signature as MethodSignature
            // evaluate element and remove un-wanted elements
            val skippedJson: Any? = skip(RESPONSE_MAPPER.convertObject(result), skipParamOutput(logMethodWithParams))
            log.info {
                "${"[Logging] Results for {}:\n {}"} ${signature.toShortString()} ${
                    RESPONSE_MAPPER.writeObject(obfuscate(skippedJson, obfuscateParamOutput(logMethodWithParams)))
                }"
            }
        }
    }

    @Suppress("LongParameterList")
    private fun prepareParamLogMessage(
        index: Int,
        paramTypes: Array<Class<*>>,
        paramNames: Array<String>,
        args: Array<Any?>,
        skipIn: Set<String>,
        obfuscateIn: Set<String>
    ): String {
        val skippedJson: Any? = skip(RESPONSE_MAPPER.convertObject(args[index]), skipIn)
        val data = if (args[index] != null && obfuscateIn.contains(paramNames[index])) OBFUSCATION
        else RESPONSE_MAPPER.writeObject(obfuscate(skippedJson, obfuscateIn))

        return "[${paramTypes[index].simpleName} ${paramNames[index]}]\n${data};\n"
    }

    private fun obfuscateParamInput(logMethodWithParams: LogMethodWithParams): Set<String> {
        return unifiedSet(logMethodWithParams.obfuscateParameters, logMethodWithParams.obfuscateParametersInput)
    }

    private fun obfuscateParamOutput(logMethodWithParams: LogMethodWithParams): Set<String> {
        return unifiedSet(logMethodWithParams.obfuscateParameters, logMethodWithParams.obfuscateParametersOutput)
    }

    private fun skipParamInput(logMethodWithParams: LogMethodWithParams): Set<String> {
        return unifiedSet(logMethodWithParams.skipParameters, logMethodWithParams.skipParametersInput)
    }

    private fun skipParamOutput(logMethodWithParams: LogMethodWithParams): Set<String> {
        return unifiedSet(logMethodWithParams.skipParameters, logMethodWithParams.skipParametersOutput)
    }

    private fun unifiedSet(a1: Array<String>, a2: Array<String>): Set<String> {
        return (a1 + a2).toSet()
    }

    companion object {
        private val log = KotlinLogging.logger {  }
        private val RESPONSE_MAPPER: ResponseMapper = ResponseMapper()
    }
}
