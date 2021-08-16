package com.cynergisuite.middleware.localization

import io.micronaut.context.i18n.ResourceBundleMessageSource
import spock.lang.Specification
import spock.lang.Unroll

class LocalizationServiceSpecification extends Specification {

   void "check english locale"() {
      given:
      final resourceBundleMessageSource = new ResourceBundleMessageSource("i18n.messages")
      final localizationService = new LocalizationService(resourceBundleMessageSource)

      when:
      final englishLocale = localizationService.localeFor("en")

      then:
      englishLocale != null
      englishLocale.toLanguageTag() == "en"
      englishLocale.getISOCountries() != null
      englishLocale.getISOCountries().contains("US")
   }

   @Unroll
   void "localize english messages"() {
      given:
      final resourceBundleMessageSource = new ResourceBundleMessageSource("i18n.messages")
      final localizationService = new LocalizationService(resourceBundleMessageSource)
      final englishLocale = localizationService.localeFor("en")

      expect:
      localizationService.localize(messageKey, englishLocale, "") == message

      where:
      messageKey          || message
      new NotNull("name") || "Is required"
      new NotFound(1)     || "1 was unable to be found"
   }

   void "localize a messageKey that can't be found" () {
      given:
      final resourceBundleMessageSource = new ResourceBundleMessageSource("i18n.messages")
      final localizationService = new LocalizationService(resourceBundleMessageSource)
      final englishLocale = localizationService.localeFor("en")

      expect:
      localizationService.localize("missing.message", englishLocale, "MISSING MESSAGE") == "MISSING MESSAGE"
   }
}
