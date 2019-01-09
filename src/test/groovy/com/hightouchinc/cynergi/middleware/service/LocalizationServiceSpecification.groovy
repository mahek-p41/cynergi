package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.validator.ErrorCodes
import org.springframework.context.support.ResourceBundleMessageSource
import spock.lang.Specification

class LocalizationServiceSpecification extends Specification {

   void "check english locale" () {
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

   void "localize english messages" () {
      given:
      final def resourceBundleMessageSource = new ResourceBundleMessageSource([basename: "i18n/messages"])
      final def localizationService = new LocalizationService(resourceBundleMessageSource)
      final def englishLocale = localizationService.localeFor("en")

      expect:
      localizationService.localize(messageKey, englishLocale, args) == message

      where:
      messageKey                     | args                 || message
      ErrorCodes.Validation.NOT_NULL | ["name"] as Object[] || "name is required"
      ErrorCodes.System.NOT_FOUND    | [1] as Object[]      || "Resource 1 was unable to be found"
   }
}
