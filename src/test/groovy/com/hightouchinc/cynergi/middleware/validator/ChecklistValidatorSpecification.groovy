package com.hightouchinc.cynergi.middleware.validator

import com.hightouchinc.cynergi.middleware.entity.ChecklistDto
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.ChecklistService
import com.hightouchinc.cynergi.test.data.loader.ChecklistTestDataLoader
import org.eclipse.collections.impl.factory.Lists
import spock.lang.Specification

class ChecklistValidatorSpecification extends Specification {

   void "validate save valid ChecklistDto" () {
      given:
      final def checklistDto = ChecklistTestDataLoader.stream(1).map { new ChecklistDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Checklist") }
      final def checkListService = Mock(ChecklistService)
      final def validator = new ChecklistValidator(checkListService)

      when:
      validator.validateSave(checklistDto, "corrto")

      then:
      1 * checkListService.exists(checklistDto.customerAccount) >> false
      notThrown(ValidationException)
   }

   void "validate save invalid ChecklistDto due to duplicate customer account" () {
      given:
      final def checklistDto = ChecklistTestDataLoader.stream(1).map { new ChecklistDto(it) }.findFirst().orElseThrow { new Exception("Unable to create Checklist") }
      final def checkListService = Mock(ChecklistService)
      final def validator = new ChecklistValidator(checkListService)

      when:
      validator.validateSave(checklistDto, "corrto")

      then:
      1 * checkListService.exists(checklistDto.customerAccount) >> true
      final def validationException = thrown(ValidationException)
      validationException.errors.size() == 1
      validationException.errors[0].arguments == Lists.immutable.of(checklistDto.customerAccount)
      validationException.errors[0].path == "cust_acct"
      validationException.errors[0].messageTemplate == ErrorCodes.Validation.DUPLICATE
   }

   void "validate update valid ChecklistDto" () {
      given:
      final def checklistDto = ChecklistTestDataLoader.stream(1).map { new ChecklistDto(it) }.peek { it.id = 1 }.findFirst().orElseThrow { new Exception("Unable to create Checklist") }
      final def toUpdateChecklistDto = checklistDto.copyMe()
      final def checkListService = Mock(ChecklistService)
      final def validator = new ChecklistValidator(checkListService)

      when:
      toUpdateChecklistDto.verifiedBy = "someoneelse"
      validator.validateUpdate(toUpdateChecklistDto, "corrto")

      then:
      1 * checkListService.fetchById(toUpdateChecklistDto.id) >> checklistDto
      notThrown(ValidationException)
   }

}
