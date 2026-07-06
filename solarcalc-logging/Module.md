# Module solarcalc-logging - solarcalc Logging

To be able to log method calls, solarcalc offers a corresponding annotation system.

The following annotations are available for this purpose:


## `@LogMethod`
Logs only the method call.

### Example output

```
INFO  io.github.raginlundf.logging.LoggingImpl.logMethodCalled 49 - [solarcalc Logging] Method MockKeycloakController.token(..) has been called
```

## `@LogDuration`

Logs the method and the time used for it.

### Example output

```
INFO  io.github.raginlundf.logging.LoggingImpl.logExecutionTime 37 - [solarcalc Logging] MockKeycloakController.token(..) execution took 72 ms
```

## `@LogMethodWithParams`
Logs the method with parameters. These can still be skipped or obfuscated accordingly.


The following parameters are available to control the parameters:

| Argument                    | Type    | Description                                                        |
|-----------------------------|---------|--------------------------------------------------------------------|
| `logInput`                  | `bool`  | Switch to activate or deactivate input parameter logging.          | 
| `logOutput`                 | `bool`  | Switch to activate or deactivate output parameter logging.         | 
| `obfuscateParameters`       | `array` | Array of parameter names to obfuscate at input and output logging. | 
| `obfuscateParametersInput`  | `array` | Array of parameter names to obfuscate at input logging.            | 
| `obfuscateParametersOutput` | `array` | Array of parameter names to obfuscate at output logging.           | 
| `skipParameters`            | `array` | Array of parameter names to skip for input and output logging.     | 
| `skipParametersInput`       | `array` | Array of parameter names to skip for input logging.                | 
| `skipParametersOutput`      | `array` | Array of parameter names to skip for output logging.               | 


### Example output (without arguments)
```
INFO  io.github.raginlundf.logging.LoggingImpl.logMethodResult 99 - [solarcalc Logging] Results for {}:
 {} MockKeycloakController.token(..) {
  "headers" : { },
  "body" : {
    "token" : "eyJraWQiOiJrZX...."
  },
  "statusCodeValue" : 200,
  "statusCode" : "OK"
}
```

### Example output (with `logOutput=false` argument)

```
INFO  io.github.raginlundf.logging.LoggingImpl.logMethodParams 78 - [solarcalc Logging] Input parameter for MockKeycloakController.token(..):[String userId]
"b846f572-6329-4ce3-a788-c005e72cba09";
```

### Example output (with `obfuscateParameters = ["token"]` argument)

```
INFO  io.github.raginlundf.logging.LoggingImpl.logMethodResult 99 - [solarcalc Logging] Results for {}:
 {} MockKeycloakController.token(..) {
  "headers" : { },
  "body" : {
    "token" : "*****"
  },
  "statusCodeValue" : 200,
  "statusCode" : "OK"
}
```
