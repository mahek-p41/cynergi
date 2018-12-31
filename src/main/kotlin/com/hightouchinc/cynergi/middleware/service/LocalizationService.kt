package com.hightouchinc.cynergi.middleware.service

import org.springframework.context.MessageSource
import java.util.*
import javax.inject.Singleton

@Singleton
class LocalizationService(
   private val messageSource: MessageSource
) {
   fun localeFor(languageTag: String): Locale? {
      return Locale.forLanguageTag(languageTag)
   }

   fun localize(messageKey: String, locale: Locale = Locale.US, vararg arguments: Any): String? {
      return messageSource.getMessage(messageKey, arguments, locale)
   }
}
