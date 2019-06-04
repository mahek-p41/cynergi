package com.cynergisuite.middleware.localization

sealed class LocalizationCode(val code: String)

sealed class Validation(code: String): LocalizationCode(code) {
   object NotNull : Validation("javax.validation.constraints.NotNull.message")
   object Size : Validation("javax.validation.constraints.Size.message")
   object Positive : Validation("javax.validation.constraints.Positive.message")
   object Min : Validation("javax.validation.constraints.Min.message")
   object Max : Validation("javax.validation.constraints.Max.message")
}

sealed class Cynergi(code: String): LocalizationCode(code) {
   object Duplicate : Cynergi("cynergi.validation.duplicate")
   object NotUpdatable : Cynergi("cynergi.validation.not.updatable")
   object EndDateBeforeStart : Cynergi("cynergi.validation.end.date.before.start")
   object NotificationRecipientsRequiredAll : Cynergi("cynergi.validation.notification.recipients.not.required")
   object NotificationRecipientsRequired : Cynergi("cynergi.validation.notification.recipients.required")
   object ConversionError : Cynergi("cynergi.conversion.error")
}

sealed class SystemCode(code: String): LocalizationCode(code) {
   object NotFound: SystemCode("system.not.found")
   object InternalError: SystemCode("system.internal.error")
   object RouteError: SystemCode("system.route.error")
   object NotImplemented: SystemCode("system.not.implemented")
   object LoggedIn: SystemCode("system.logged.in")
   object NotLoggedIn: SystemCode("system.not.logged.in")
   object AccessDenied: SystemCode("system.access.denied")
   object Unknown: SystemCode("system.word.unknown")
   object UnableToParseJson: SystemCode("system.json.unable.parse")
   object PageOutOfBounds: SystemCode("system.page.out.of.bounds")
}
