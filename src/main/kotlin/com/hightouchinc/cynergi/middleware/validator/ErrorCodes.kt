package com.hightouchinc.cynergi.middleware.validator

object ErrorCodes {
   object Validation {
      const val NOT_NULL = "validation.not.null"
      const val DUPLICATE = "validation.duplicate"
      const val NOT_UPDATABLE = "validation.not.updatable"
      const val POSITIVE_NUMBER_REQUIRED = "validation.positive.number.required"
      const val SIZE = "values between {0} and {1} are not valid sizes"
   }

   object System {
      const val NOT_FOUND = "system.not.found"
      const val INTERNAL_ERROR = "system.internal.error"
   }
}
