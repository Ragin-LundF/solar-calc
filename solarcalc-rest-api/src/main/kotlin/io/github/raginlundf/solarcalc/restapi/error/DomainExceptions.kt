package io.github.raginlundf.solarcalc.restapi.error

class ResourceNotFoundException(message: String) : RuntimeException(message)

class TenantAccessDeniedException(message: String) : RuntimeException(message)
