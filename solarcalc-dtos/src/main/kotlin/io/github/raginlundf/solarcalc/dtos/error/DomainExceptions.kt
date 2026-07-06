package io.github.raginlundf.solarcalc.dtos.error

class ResourceNotFoundException(message: String) : RuntimeException(message)

class UsernameAlreadyExistsException(message: String) : RuntimeException(message)

class InvalidCredentialsException(message: String) : RuntimeException(message)
