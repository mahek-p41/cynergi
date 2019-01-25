package com.hightouchinc.cynergi.middleware.validator

object ErrorCodes {
   object Validation {
      const val NOT_NULL = "javax.validation.not.null"
      const val SIZE = "javax.validation.size"
   }

   object Cynergi {
      const val DUPLICATE = "cynergi.validation.duplicate"
      const val NOT_UPDATABLE = "cynergi.validation.not.updatable"
      const val POSITIVE_NUMBER_REQUIRED = "cynergi.validation.positive.number.required"
   }

   object System {
      const val NOT_FOUND = "system.not.found"
      const val INTERNAL_ERROR = "system.internal.error"
      const val REQUIRED_ARGUMENT = "system.route.error"
      const val NOT_IMPLEMENTED = "system.not.implemented"
   }
}
