package com.cynergisuite.middleware.error

import com.cynergisuite.middleware.localization.LocalizationCode

class ValidationException(
   val errors: Set<ValidationError>
) : Exception(
   errors.asSequence()
      .map { "${it.localizationCode.getCode()}:${it.localizationCode.getArguments().joinToString("\$")}->${it.path}" }
      .joinToString(",")
)

data class ValidationError(
   val path: String?,
   val localizationCode: LocalizationCode
)
