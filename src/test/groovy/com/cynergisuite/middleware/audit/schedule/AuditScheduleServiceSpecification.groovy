package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.department.DepartmentFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.store.StoreFactory
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject
import java.time.DayOfWeek

@MicronautTest(transactional = false)
class AuditScheduleServiceSpecification extends ServiceSpecificationBase {
   @Inject AuditFactoryService auditFactoryService
   @Inject AuditScheduleFactoryService auditScheduleFactoryService
   @Inject AuditScheduleService auditScheduleService
   @Inject EmployeeFactoryService employeeFactoryService

   void "one store test"() {
      given:
      final store = StoreFactory.random()
      final dept = DepartmentFactory.random()
      final employee = employeeFactoryService.single(store)
      final schedule = auditScheduleFactoryService.single(DayOfWeek.MONDAY, [store], dept, employee)

      when:
      def result = auditScheduleService.processSchedule(schedule)

      then:
      notThrown(ValidationException)
      result.size() == 1

      result.notifications[0].message == schedule.description
      result.notifications[0].sendingEmployee == employee.number.toString()
      result.notifications[0].expirationDate != null
   }

   void "two store test"() {
      given:
      final store1 = StoreFactory.storeOne()
      final store3 = StoreFactory.storeThree()
      final dept = DepartmentFactory.random()
      final employee = employeeFactoryService.single(store1)
      final schedule = auditScheduleFactoryService.single(DayOfWeek.FRIDAY, [store1, store3], dept, employee)

      when:
      def result = auditScheduleService.processSchedule(schedule)

      then:
      notThrown(ValidationException)
      result.notifications.size() == 2

      result.notifications[0].message == schedule.description
      result.notifications[0].sendingEmployee == employee.number.toString()
      result.notifications[0].expirationDate != null

      result.notifications[1].message == schedule.description
      result.notifications[1].sendingEmployee == employee.number.toString()
      result.notifications[1].expirationDate != null
   }

   void "one store with already CREATED audit" () {
      given:
      final store1 = StoreFactory.storeOne()
      final dept = DepartmentFactory.random()
      final employee = employeeFactoryService.single(store1)
      final createdAudit = auditFactoryService.single(store1, employee, [AuditStatusFactory.created()] as Set)
      final schedule = auditScheduleFactoryService.single(DayOfWeek.MONDAY, [store], dept, employee)

      when:
      def result = auditScheduleService.processSchedule(schedule)

      then:
      notThrown(Exception)
      result.notifications.size() == 1

      result.notifications[0].message == schedule.description
      result.notifications[0].sendingEmployee == employee.number.toString()
      result.notifications[0].expirationDate != null
   }
}
