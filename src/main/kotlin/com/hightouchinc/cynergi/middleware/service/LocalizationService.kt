package com.hightouchinc.cynergi.middleware.service

import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Defines the localization service that is to be used by the system.  The idea for this is that when a message is to
 * be returned by the system that it will be localized using appropriate message.(language).properties file
 *
 * @property messageSource the configured [MessageSource] that will be used to localize the messages
 *
 * @author garym@hightouchinc.com
 */
@Singleton
class LocalizationService @Inject constructor(
   private val messageSource: MessageSource
) {
   private companion object {
       val logger: Logger = LoggerFactory.getLogger(LocalizationService::class.java)
   }

   /**
    * Loads a [Locale] based on the language tag.  This should be most likely retrieved from the Accept-Language HTTP header
    *
    * @return a [Locale] based on the tag or null if one can't be found
    */
   fun localeFor(languageTag: String): Locale? {
      return Locale.forLanguageTag(languageTag)
   }

   /**
    * localizes a message based on a key using the provided [Locale] or [Locale.US] if one is provided
    *
    * @param messageKey the key of the message to be looked up and localized
    * @param locale the [Locale] instance to be used to localize the message or [Locale.US] as a default
    * @param arguments the arguments to be provided to the messageSource
    * @return [String] that is the localized message with the arguments applied using the provided [Locale] or the Java empty string if an error of some kind occurs
    */
   fun localize(messageKey: String, locale: Locale = Locale.US, vararg arguments: Any): String {
      return try {
         messageSource.getMessage(messageKey, arguments, locale)
      } catch (e: Throwable) {
         logger.error("Unable to convert message using {} for locale {} with arguments {}", messageKey, locale, arguments)
         EMPTY // return the Java empty string if an error occurred
      }
   }
}
