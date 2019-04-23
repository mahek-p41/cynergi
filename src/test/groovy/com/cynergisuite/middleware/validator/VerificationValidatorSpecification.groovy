package com.cynergisuite.middleware.validator


import com.cynergisuite.middleware.localization.MessageCodes
import spock.lang.Specification

class VerificationValidatorSpecification extends Specification {

   void "validate save valid VerificationDto" () {
      given:
      final def verificationDto = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1).map { new com.cynergisuite.middleware.entity.VerificationDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def verificationService = Mock(com.cynergisuite.middleware.service.VerificationService)
      final def validator = new VerificationValidator(verificationService)

      when:
      validator.validateSave(verificationDto, "corrto")

      then:
      1 * verificationService.exists(verificationDto.customerAccount) >> false
      notThrown(com.cynergisuite.middleware.exception.ValidationException)
   }

   void "validate save invalid VerificationDto due to duplicate customer account" () {
      given:
      final def verificationDto = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1).map { new com.cynergisuite.middleware.entity.VerificationDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def verificationService = Mock(com.cynergisuite.middleware.service.VerificationService)
      final def validator = new VerificationValidator(verificationService)

      when:
      validator.validateSave(verificationDto, "corrto")

      then:
      1 * verificationService.exists(verificationDto.customerAccount) >> true
      final def validationException = thrown(com.cynergisuite.middleware.exception.ValidationException)
      validationException.errors.size() == 1
      validationException.errors[0].arguments == [verificationDto.customerAccount]
      validationException.errors[0].path == "cust_acct"
      validationException.errors[0].messageTemplate == MessageCodes.Cynergi.DUPLICATE
   }

   void "validate update valid VerificationDto" () {
      given:
      final def verificationDto = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1).map { new com.cynergisuite.middleware.entity.VerificationDto(it) }.peek { it.id = 1 }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def toUpdateVerificationDto = verificationDto.copyMe()
      final def verificationService = Mock(com.cynergisuite.middleware.service.VerificationService)
      final def validator = new VerificationValidator(verificationService)

      when:
      toUpdateVerificationDto.verifiedBy = "someoneelse"
      validator.validateUpdate(toUpdateVerificationDto, "corrto")

      then:
      1 * verificationService.fetchById(toUpdateVerificationDto.id) >> verificationDto
      notThrown(com.cynergisuite.middleware.exception.ValidationException)
   }
}
