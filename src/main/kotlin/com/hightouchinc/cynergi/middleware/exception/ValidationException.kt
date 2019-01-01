package com.hightouchinc.cynergi.middleware.exception

class ValidationException(
   val errors: List<ValidationError>
): Exception()

data class ValidationError(
   val path: String,
   val messageTemplate: String,
   val arguments: List<Any> = listOf()
)
