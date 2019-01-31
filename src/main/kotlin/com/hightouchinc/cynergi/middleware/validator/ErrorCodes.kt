package com.hightouchinc.cynergi.middleware.validator

object ErrorCodes {
   object Validation {
      // define validation messages here that use the provided javax.validation annotations ex: @NotNull @Size
      const val NOT_NULL = "javax.validation.not.null"
      const val SIZE = "javax.validation.size"
   }

   object Cynergi {
      const val DUPLICATE = "cynergi.validation.duplicate"
      const val NOT_UPDATABLE = "cynergi.validation.not.updatable"
      const val END_DATE_BEFORE_START = "cynergi.validation.end.date.before.start"
      const val POSITIVE_NUMBER_REQUIRED = "cynergi.validation.positive.number.required"
      const val NOTIFICATION_RECIPIENTS_ALL = "cynergi.validation.notification.recipients.not.required"
      const val NOTIFICATION_RECIPIENTS_REQUIRED = "cynergi.validation.notification.recipients.required"
   }

   object System {
      const val NOT_FOUND = "system.not.found"
      const val INTERNAL_ERROR = "system.internal.error"
      const val REQUIRED_ARGUMENT = "system.route.error"
      const val NOT_IMPLEMENTED = "system.not.implemented"
   }
}
