package com.hightouchinc.cynergi.middleware.validator

object ErrorCodes {
   object Validation {
      const val NOT_NULL = "validation.not.null"
      const val DUPLICATE = "validation.duplicate"
      const val NOT_UPDATABLE = "validation.not.updatable"
   }

   object System {
      const val NOT_FOUND = "system.not.found"
      const val INTERNAL_ERROR = "system.internal.error"
   }
}
