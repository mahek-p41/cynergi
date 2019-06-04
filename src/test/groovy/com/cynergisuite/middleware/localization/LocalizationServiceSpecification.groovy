package com.cynergisuite.middleware.localization

import org.springframework.context.support.ResourceBundleMessageSource
import spock.lang.Specification

import static com.cynergisuite.middleware.localization.SystemCode.NotFound
import static com.cynergisuite.middleware.localization.Validation.NotNull

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
      messageKey        | args                 || message
      NotNull.INSTANCE  | ["name"] as Object[] || "name is required"
      NotFound.INSTANCE | [1] as Object[]      || "Resource 1 was unable to be found"
      "i.dont.exist"    | []                   || ""
   }
}
