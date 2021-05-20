package com.cynergisuite.middleware.verification

import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.Duplicate
import com.cynergisuite.middleware.verfication.VerificationService
import com.cynergisuite.middleware.verfication.VerificationValidator
import com.cynergisuite.middleware.verfication.VerificationValueObject
import spock.lang.Specification

class VerificationValidatorSpecification extends Specification {

   void "validate save valid VerificationValueObject" () {
      given:
      final verificationValueObject = VerificationTestDataLoader.stream(1).map { new VerificationValueObject(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final verificationService = Mock(VerificationService)
      final validator = new VerificationValidator(verificationService)

      when:
      validator.validateCreate(verificationValueObject, "corrto")

      then:
      1 * verificationService.exists(verificationValueObject.customerAccount) >> false
      notThrown(ValidationException)
   }

   void "validate save invalid VerificationValueObject due to duplicate customer account" () {
      given:
      final verificationValueObject = VerificationTestDataLoader.stream(1).map { new VerificationValueObject(it) }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final verificationService = Mock(VerificationService)
      final validator = new VerificationValidator(verificationService)

      when:
      validator.validateCreate(verificationValueObject, "corrto")

      then:
      1 * verificationService.exists(verificationValueObject.customerAccount) >> true
      final validationException = thrown(ValidationException)
      validationException.errors.size() == 1
      validationException.errors[0].localizationCode.arguments.size() == 1
      validationException.errors[0].localizationCode.arguments[0] == verificationValueObject.customerAccount
      validationException.errors[0].path == "cust_acct"
      validationException.errors[0].localizationCode instanceof Duplicate
   }

   void "validate update valid VerificationValueObject" () {
      given:
      final verificationValueObject = VerificationTestDataLoader.stream(1).map { new VerificationValueObject(it) }.peek { it.id = 1 }.findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final toUpdateVerificationValueObject = verificationValueObject.copyMe()
      final verificationService = Mock(VerificationService)
      final validator = new VerificationValidator(verificationService)

      when:
      toUpdateVerificationValueObject.verifiedBy = "someoneelse"
      validator.validateUpdate(toUpdateVerificationValueObject, "corrto")

      then:
      1 * verificationService.fetchById(toUpdateVerificationValueObject.id) >> verificationValueObject
      notThrown(ValidationException)
   }
}
