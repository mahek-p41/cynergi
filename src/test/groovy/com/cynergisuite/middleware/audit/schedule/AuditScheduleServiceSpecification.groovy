package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.department.DepartmentFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.store.StoreFactory
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject
import java.time.DayOfWeek

@MicronautTest(transactional = false)
class AuditScheduleServiceSpecification extends ServiceSpecificationBase {
   @Inject AuditScheduleFactoryService auditScheduleFactoryService
   @Inject AuditScheduleService auditScheduleService
   @Inject EmployeeFactoryService employeeFactoryService

   void "one store test"() {
      setup:
      final store = StoreFactory.random()
      final dept = DepartmentFactory.random()
      final employee = employeeFactoryService.single(store)
      final schedule = auditScheduleFactoryService.single(DayOfWeek.MONDAY, [store], dept, employee)

      when:
      def result = auditScheduleService.createNotificationAndAudit(schedule)

      then:
      notThrown(ValidationException)
      result.size() == 1

      result[0].message == schedule.description
      result[0].sendingEmployee == employee.number.toString()
      result[0].expirationDate != null
   }

   void "two store test"() {
      setup:
      final store1 = StoreFactory.storeOne()
      final store3 = StoreFactory.storeThree()
      final dept = DepartmentFactory.random()
      final employee = employeeFactoryService.single(store1)
      final schedule = auditScheduleFactoryService.single(DayOfWeek.FRIDAY, [store1, store3], dept, employee)

      when:
      def result = auditScheduleService.createNotificationAndAudit(schedule)

      then:
      notThrown(ValidationException)
      result.size() == 2

      result[0].message == schedule.description
      result[0].sendingEmployee == employee.number.toString()
      result[0].expirationDate != null

      result[1].message == schedule.description
      result[1].sendingEmployee == employee.number.toString()
      result[1].expirationDate != null

   }

}






