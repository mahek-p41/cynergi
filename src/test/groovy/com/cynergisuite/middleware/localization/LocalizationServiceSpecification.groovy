package com.cynergisuite.middleware.localization

import org.springframework.context.support.ResourceBundleMessageSource
import spock.lang.Specification

import static com.cynergisuite.middleware.localization.MessageCodes.System.NOT_FOUND
import static com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL

class LocalizationServiceSpecification extends Specification {

   void "check english locale"() {
      given:
      final def resourceBundleMessageSource = new ResourceBundleMessageSource([basename: "i18n/messages"])
      final def localizationService = new LocalizationService(resourceBundleMessageSource)

      when:
      final def englishLocale = localizationService.localeFor("en")

      then:
      englishLocale != null
      englishLocale.toLanguageTag() == "en"
      englishLocale.getISOCountries() != null
      englishLocale.getISOCountries().contains("US")
   }

   void "localize english messages"() {
      given:
      final def resourceBundleMessageSource = new ResourceBundleMessageSource([basename: "i18n/messages"])
      final def localizationService = new LocalizationService(resourceBundleMessageSource)
      final def englishLocale = localizationService.localeFor("en")

      expect:
      localizationService.localize(messageKey, englishLocale, "", args) == message

      where:
      messageKey     | args                 || message
      NOT_NULL       | ["name"] as Object[] || "name is required"
      NOT_FOUND      | [1] as Object[]      || "Resource 1 was unable to be found"
      "i.dont.exist" | []                   || ""
   }
}
