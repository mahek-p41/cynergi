package com.cynergisuite.middleware.verification.infrastructure

import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.MessageCodes
import com.cynergisuite.middleware.verfication.VerificationDto
import com.cynergisuite.middleware.verfication.infrastructure.VerificationService
import com.cynergisuite.middleware.verfication.infrastructure.VerificationValidator
import spock.lang.Specification

class VerificationValidatorSpecification extends Specification {

   void "validate save valid VerificationDto" () {
      given:
      final def verificationDto = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1).map { new VerificationDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def verificationService = Mock(VerificationService)
      final def validator = new VerificationValidator(verificationService)

      when:
      validator.validateSave(verificationDto, "corrto")

      then:
      1 * verificationService.exists(verificationDto.customerAccount) >> false
      notThrown(ValidationException)
   }

   void "validate save invalid VerificationDto due to duplicate customer account" () {
      given:
      final def verificationDto = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1).map { new VerificationDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def verificationService = Mock(VerificationService)
      final def validator = new VerificationValidator(verificationService)

      when:
      validator.validateSave(verificationDto, "corrto")

      then:
      1 * verificationService.exists(verificationDto.customerAccount) >> true
      final def validationException = thrown(ValidationException)
      validationException.errors.size() == 1
      validationException.errors[0].arguments == [verificationDto.customerAccount]
      validationException.errors[0].path == "cust_acct"
      validationException.errors[0].messageTemplate == MessageCodes.Cynergi.DUPLICATE
   }

   void "validate update valid VerificationDto" () {
      given:
      final def verificationDto = com.cynergisuite.test.data.loader.VerificationTestDataLoader.stream(1).map { new VerificationDto(it) }.peek { it.id = 1 }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def toUpdateVerificationDto = verificationDto.copyMe()
      final def verificationService = Mock(VerificationService)
      final def validator = new VerificationValidator(verificationService)

      when:
      toUpdateVerificationDto.verifiedBy = "someoneelse"
      validator.validateUpdate(toUpdateVerificationDto, "corrto")

      then:
      1 * verificationService.fetchById(toUpdateVerificationDto.id) >> verificationDto
      notThrown(ValidationException)
   }
}
