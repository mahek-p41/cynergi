package com.cynergisuite.middleware.localization

import com.cynergisuite.middleware.error.ErrorDTO
import io.micronaut.context.MessageSource
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
   private val logger: Logger = LoggerFactory.getLogger(LocalizationService::class.java)

   /**
    * Loads a [Locale] based on the language tag.  This should be most likely retrieved from the Accept-Language HTTP header
    *
    * @return a [Locale] based on the tag or null if one can't be found
    */
   fun localeFor(languageTag: String): Locale? {
      return Locale.forLanguageTag(languageTag)
   }

   /**
    * Helper method for converting errors to ErrorDataTransferObject's
    */
   fun localizeError(localizationCode: LocalizationCode, locale: Locale, path: String? = null): ErrorDTO =
      ErrorDTO(
         message = localize(localizationCode, locale),
         path = path,
         code = localizationCode.getCode()
      )

   /**
    * localizes a message based on a key using the provided [Locale] or [Locale.US] if one is provided.  Assumption with
    * this method is that the message does not take any parameters.
    *
    * @param messageKey the key of the message to be looked up and localized
    * @param locale the [Locale] instance to be used to localize the message or [Locale.US] as a default
    * @return [String] that is the localized message with the arguments applied using the provided [Locale] or the Java empty string if an error of some kind occurs
    */
   fun localize(messageKey: String, locale: Locale = Locale.US, ifNotFound: String = EMPTY): String =
      localize(messageKey = messageKey, locale = locale, ifNotFound = ifNotFound, arguments = emptyArray())

   /**
    * localizes a message based on a key using the provided [Locale] or [Locale.US] if one is provided
    *
    * @param localizationCode the key of the message to be looked up and localized
    * @param locale the [Locale] instance to be used to localize the message or [Locale.US] as a default
    * @return [String] that is the localized message with the arguments applied using the provided [Locale] or the Java empty string if an error of some kind occurs
    */
   fun localize(localizationCode: LocalizationCode, locale: Locale = Locale.US, ifNotFound: String = EMPTY): String =
      localize(localizationCode.getCode(), locale, ifNotFound, localizationCode.getArguments())

   /**
    * localizes a message based on a key using the provided [Locale] or [Locale.US] if one is provided
    * Prefer the version that takes a map instead of the array.
    *
    * @param messageKey the key of the message to be looked up and localized
    * @param locale the [Locale] instance to be used to localize the message or [Locale.US] as a default
    * @param arguments the arguments to be provided to the messageSource
    * @return [String] that is the localized message with the arguments applied using the provided [Locale] or the Java empty string if an error of some kind occurs
    */
   fun localize(messageKey: String, locale: Locale = Locale.US, ifNotFound: String = EMPTY, arguments: Array<Any?>): String {
      return localize(
         messageKey, locale, ifNotFound,
         arguments.asSequence()
            .filter { arg -> arg != null }
            .mapIndexed { index, arg -> index.toString() to arg }
            .associate { it }
      )
   }

   /**
    * localizes a message based on a key using the provided [Locale] or [Locale.US] if one is provided
    *
    * @param messageKey the key of the message to be looked up and localized
    * @param locale the [Locale] instance to be used to localize the message or [Locale.US] as a default
    * @param arguments the arguments to be provided to the messageSource
    * @return [String] that is the localized message with the arguments applied using the provided [Locale] or the Java empty string if an error of some kind occurs
    */
   fun localize(messageKey: String, locale: Locale = Locale.US, ifNotFound: String = EMPTY, arguments: Map<String, Any?> = emptyMap()): String {
      val cleanMessageKey = messageKey.removeSurrounding("{", "}")
      val messageContext = MessageSource.MessageContext.of(locale, arguments)

      return try {
         messageSource.getMessage(cleanMessageKey, messageContext, ifNotFound)
      } catch (e: Throwable) {
         logger.trace("Conversion error", e)
         logger.error("Unable to convert message using {} for locale {} with arguments {}", messageKey, locale, arguments)
         ifNotFound // return the Java empty string if an error occurred
      }
   }
}
