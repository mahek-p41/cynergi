package com.cynergisuite.middleware.verification

import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.MessageCodes
import com.cynergisuite.middleware.verfication.VerificationValueObject
import com.cynergisuite.middleware.verfication.VerificationTestDataLoader
import com.cynergisuite.middleware.verfication.VerificationService
import com.cynergisuite.middleware.verfication.VerificationValidator
import spock.lang.Specification

class VerificationValidatorSpecification extends Specification {

   void "validate save valid VerificationValueObject" () {
      given:
      final def verificationValueObject = VerificationTestDataLoader.stream(1).map { new VerificationValueObject(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def verificationService = Mock(VerificationService)
      final def validator = new VerificationValidator(verificationService)

      when:
      validator.validateSave(verificationValueObject, "corrto")

      then:
      1 * verificationService.exists(verificationValueObject.customerAccount) >> false
      notThrown(ValidationException)
   }

   void "validate save invalid VerificationValueObject due to duplicate customer account" () {
      given:
      final def verificationValueObject = VerificationTestDataLoader.stream(1).map { new VerificationValueObject(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def verificationService = Mock(VerificationService)
      final def validator = new VerificationValidator(verificationService)

      when:
      validator.validateSave(verificationValueObject, "corrto")

      then:
      1 * verificationService.exists(verificationValueObject.customerAccount) >> true
      final def validationException = thrown(ValidationException)
      validationException.errors.size() == 1
      validationException.errors[0].arguments == [verificationValueObject.customerAccount]
      validationException.errors[0].path == "cust_acct"
      validationException.errors[0].messageTemplate == MessageCodes.Cynergi.DUPLICATE
   }

   void "validate update valid VerificationValueObject" () {
      given:
      final def verificationValueObject = VerificationTestDataLoader.stream(1).map { new VerificationValueObject(it) }.peek { it.id = 1 }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final def toUpdateVerificationValueObject = verificationValueObject.copyMe()
      final def verificationService = Mock(VerificationService)
      final def validator = new VerificationValidator(verificationService)

      when:
      toUpdateVerificationValueObject.verifiedBy = "someoneelse"
      validator.validateUpdate(toUpdateVerificationValueObject, "corrto")

      then:
      1 * verificationService.fetchById(toUpdateVerificationValueObject.id) >> verificationValueObject
      notThrown(ValidationException)
   }
}
