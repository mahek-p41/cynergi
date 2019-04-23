package com.hightouchinc.cynergi.middleware.localization

object MessageCodes {
   object Validation {
      // define validation messages here that use the provided javax.validation annotations ex: @NotNull @Size
      const val NOT_NULL = "javax.validation.not.null"
      const val SIZE = "javax.validation.size"
      const val MIN = "javax.validation.constraints.Min.message"
      const val MAX = "javax.validation.constraints.Max.message"
   }

   object Cynergi {
      const val DUPLICATE = "cynergi.validation.duplicate"
      const val NOT_UPDATABLE = "cynergi.validation.not.updatable"
      const val END_DATE_BEFORE_START = "cynergi.validation.end.date.before.start"
      const val POSITIVE_NUMBER_REQUIRED = "cynergi.validation.positive.number.required"
      const val NOTIFICATION_RECIPIENTS_ALL = "cynergi.validation.notification.recipients.not.required"
      const val NOTIFICATION_RECIPIENTS_REQUIRED = "cynergi.validation.notification.recipients.required"
      const val CONVERSION_ERROR = "cynergi.conversion.error"
      const val LEVEL_NOT_FOUND = "cynergi.level.not.found"
   }

   object System {
      const val NOT_FOUND = "system.not.found"
      const val INTERNAL_ERROR = "system.internal.error"
      const val REQUIRED_ARGUMENT = "system.route.error"
      const val NOT_IMPLEMENTED = "system.not.implemented"
      const val LOGGED_IN = "system.logged.in"
      const val NOT_LOGGED_IN = "system.not.logged.in"
   }
}
