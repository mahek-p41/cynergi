package com.cynergisuite.common.error

import com.cynergisuite.middleware.localization.LocalizationCode

class ValidationException(
   val errors: Set<ValidationError>
) : Exception(
   errors.asSequence()
      .map { "${it.localizationCode.getCode()}:${it.localizationCode.getArguments().joinToString("\$")}->${it.path}" }
      .joinToString(",")
) {
   constructor(error: ValidationError) :
      this(setOf(error))
}

data class ValidationError(
   val path: String? = null,
   val localizationCode: LocalizationCode
)