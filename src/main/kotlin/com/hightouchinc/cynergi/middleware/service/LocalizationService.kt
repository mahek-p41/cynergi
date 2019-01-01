package com.hightouchinc.cynergi.middleware.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import java.util.*
import javax.inject.Singleton

@Singleton
class LocalizationService(
   private val messageSource: MessageSource
) {
   private companion object {
       val logger: Logger = LoggerFactory.getLogger(LocalizationService::class.java)
   }
   fun localeFor(languageTag: String): Locale? {
      return Locale.forLanguageTag(languageTag)
   }

   fun localize(messageKey: String, locale: Locale = Locale.US, vararg arguments: Any): String {
      return try {
         messageSource.getMessage(messageKey, arguments, locale)
      } catch (e: Throwable) {
         logger.error("Unable to convert message using {} for locale {} with arguments {}", messageKey, locale, arguments)
         "unknown"
      }
   }
}
