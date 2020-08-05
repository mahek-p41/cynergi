package com.cynergisuite.middleware.error

import com.cynergisuite.middleware.localization.LocalizationCode

class ValidationException(
   val errors: Set<ValidationError>
) : Exception()

data class ValidationError(
   val path: String?,
   val localizationCode: LocalizationCode
)
