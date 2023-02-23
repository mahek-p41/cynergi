package com.cynergisuite.middleware.error

import com.cynergisuite.middleware.localization.LocalizationCode

class ValidationException(
   val errors: Set<ValidationError>
) : Exception() {
   constructor(error: ValidationError) :
      this(setOf(error))
}

data class ValidationError(
   val path: String? = null,
   val localizationCode: LocalizationCode
)
