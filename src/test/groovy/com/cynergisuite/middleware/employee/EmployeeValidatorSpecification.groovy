package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.ValidationError
import com.cynergisuite.middleware.error.ValidationException
import spock.lang.Specification

import static com.cynergisuite.middleware.localization.MessageCodes.System.NOT_FOUND

class EmployeeValidatorSpecification extends Specification {

   void "valid employee to be saved" () {
      given:
      def employeeRepository = Mock(EmployeeRepository)
      def employeeValidator = new EmployeeValidator(employeeRepository)

      when:
      employeeValidator.validateSave(new EmployeeValueObject("989", "studio", true))

      then:
      notThrown(ValidationException)
   }

   void "invalid employee with all properties null to be saved" () {
      given:
      def employeeRepository = Mock(EmployeeRepository)
      def employeeValidator = new EmployeeValidator(employeeRepository)

      when:
      employeeValidator.validateSave(new EmployeeValueObject(null,null, null, null))

      then:
      notThrown(ValidationException)
      // there aren't currently any checks in the validation for save where the properties are null because
      // that is checked by the javax.validation annotations.  This will need to be updated when that is no
      // longer the case
   }

   void "valid employee to be updated" () {
      given:
      def employeeRepository = Mock(EmployeeRepository)
      def employeeValidator = new EmployeeValidator(employeeRepository)

      when:
      employeeValidator.validateUpdate(new EmployeeValueObject(1L, "989", "studio", true))

      then:
      1 * employeeRepository.exists(1L) >> true
      notThrown(ValidationException)
   }

   void "valid employee to be update that doesn't exist" () {
      given:
      def employeeRepository = Mock(EmployeeRepository)
      def employeeValidator = new EmployeeValidator(employeeRepository)

      when:
      employeeValidator.validateUpdate(new EmployeeValueObject(1L, "989", "studio", true))

      then:
      1 * employeeRepository.exists(1L) >> false
      def exception = thrown(ValidationException)
      1 == exception.errors.size()
      exception.errors.containsAll([
          new ValidationError("id", NOT_FOUND, [1L])
      ])
   }

   void "invalid employee to be updated that does exist" () {
      given:
      def employeeRepository = Mock(EmployeeRepository)
      def employeeValidator = new EmployeeValidator(employeeRepository)

      when:
      employeeValidator.validateUpdate(new EmployeeValueObject(1L,null, null, null))

      then:
      1 * employeeRepository.exists(1L) >> true
      notThrown(ValidationException)
   }
}
