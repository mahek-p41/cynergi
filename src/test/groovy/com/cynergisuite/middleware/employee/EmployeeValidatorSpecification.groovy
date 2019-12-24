package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.localization.NotFound
import com.cynergisuite.middleware.store.StoreFactory
import com.cynergisuite.middleware.store.StoreValueObject
import spock.lang.Specification

class EmployeeValidatorSpecification extends Specification {

   void "valid employee to be saved"() {
      given:
      def employeeRepository = Mock(EmployeeRepository)
      def employeeValidator = new EmployeeValidator(employeeRepository)
      def store = StoreFactory.random().with { new StoreValueObject(it) }

      when:
      employeeValidator.validateCreate(new EmployeeValueObject([type: "eli", number: 989, lastName: "user", firstNameMi: "test", passCode: "studio", store: store, active: true]))

      then:
      notThrown(ValidationException)
   }

   void "invalid employee with all properties null to be saved"() {
      given:
      def employeeRepository = Mock(EmployeeRepository)
      def employeeValidator = new EmployeeValidator(employeeRepository)

      when:
      employeeValidator.validateCreate(new EmployeeValueObject(null, null, null, null, null, null, null, null, null, null))

      then:
      notThrown(ValidationException)
      // there aren't currently any checks in the validation for save where the properties are null because
      // that is checked by the javax.validation annotations.  This will need to be updated when that is no
      // longer the case
   }

   void "valid employee to be updated"() {
      given:
      def employeeRepository = Mock(EmployeeRepository)
      def employeeValidator = new EmployeeValidator(employeeRepository)
      def store = StoreFactory.random().with { new StoreValueObject(it) }

      when:
      employeeValidator.validateUpdate(new EmployeeValueObject([id: 1L, type: "eli", number: 989, lastName: "user", firstNameMi: "test", passCode: "studio", store: store, active: true]))

      then:
      1 * employeeRepository.exists(1L, "eli") >> true
      notThrown(ValidationException)
   }

   void "valid employee to be update that doesn't exist"() {
      given:
      def employeeRepository = Mock(EmployeeRepository)
      def employeeValidator = new EmployeeValidator(employeeRepository)
      def store = StoreFactory.random().with { new StoreValueObject(it) }

      when:
      employeeValidator.validateUpdate(new EmployeeValueObject([id: 1L, type: "eli", number: 989, lastName: "user", firstNameMi: "test", passCode: "studio", store: store, active: true]))

      then:
      1 * employeeRepository.exists(1L, "eli") >> false
      def exception = thrown(ValidationException)
      exception.errors.size() == 1
      exception.errors[0].localizationCode instanceof NotFound
      exception.errors[0].localizationCode.arguments.size() == 1
      exception.errors[0].localizationCode.arguments[0] == 1L
      exception.errors[0].path == "id"
   }

   void "invalid employee to be updated that does exist"() {
      given:
      def employeeRepository = Mock(EmployeeRepository)
      def employeeValidator = new EmployeeValidator(employeeRepository)
      def store = StoreFactory.random().with { new StoreValueObject(it) }

      when:
      employeeValidator.validateUpdate(new EmployeeValueObject([id: 1L, type: "eli", number: 989, lastName: "user", firstNameMi: "test", passCode: "studio", store: store, active: true]))

      then:
      1 * employeeRepository.exists(1L, "eli") >> true
      notThrown(ValidationException)
   }
}
