package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.store.StoreFactory
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static java.time.DayOfWeek.FRIDAY
import static java.time.DayOfWeek.MONDAY

@MicronautTest(transactional = false)
class AuditScheduleServiceSpecification extends ServiceSpecificationBase {
   @Inject AuditFactoryService auditFactoryService
   @Inject AuditScheduleFactoryService auditScheduleFactoryService
   @Inject AuditScheduleService auditScheduleService
   @Inject EmployeeFactoryService employeeFactoryService

   void "one store test"() {
      given:
      final store = StoreFactory.random()
      final employee = employeeFactoryService.single(store)
      final schedule = auditScheduleFactoryService.single(MONDAY, [store], employee, "tstds1")

      when:
      def result = auditScheduleService.processDaily(schedule)

      then:
      notThrown(ValidationException)
      result.notifications.size() == 1
      result.audits.size() == 1

      result.notifications[0].message == schedule.description
      result.notifications[0].sendingEmployee == employee.number.toString()
      result.notifications[0].expirationDate != null
      result.audits[0].actions.size() == 1
      result.audits[0].auditNumber > 0
      result.audits[0].currentStatus.value == "CREATED"
      result.audits[0].store.number == store.number
   }

   void "two store test"() {
      given:
      final store1 = StoreFactory.storeOne()
      final store3 = StoreFactory.storeThree()
      final employee = employeeFactoryService.single(store1)
      final schedule = auditScheduleFactoryService.single(FRIDAY, [store1, store3], employee, "tstds1")

      when:
      def result = auditScheduleService.processDaily(schedule)

      then:
      notThrown(ValidationException)
      result.notifications.size() == 2

      result.notifications[0].message == schedule.description
      result.notifications[0].sendingEmployee == employee.number.toString()
      result.notifications[0].expirationDate != null
      result.audits[0].actions.size() == 1
      result.audits[0].auditNumber > 0
      result.audits[0].currentStatus.value == "CREATED"
      result.audits[0].store.number == store1.number

      result.notifications[1].message == schedule.description
      result.notifications[1].sendingEmployee == employee.number.toString()
      result.notifications[1].expirationDate != null
      result.audits[1].actions.size() == 1
      result.audits[1].auditNumber > 0
      result.audits[1].currentStatus.value == "CREATED"
      result.audits[1].store.number == store3.number
   }

   void "one store with already CREATED audit" () {
      given:
      final store1 = StoreFactory.storeOne()
      final employee = employeeFactoryService.single(store1)
      final createdAudit = auditFactoryService.single(store1, employee, [AuditStatusFactory.created()] as Set)
      final schedule = auditScheduleFactoryService.single(MONDAY, [store1], employee, "tstds1")

      when:
      def result = auditScheduleService.processDaily(schedule)

      then:
      notThrown(Exception)
      result.notifications.size() == 1
      result.audits.size() == 1

      result.notifications[0].message == schedule.description
      result.notifications[0].sendingEmployee == employee.number.toString()
      result.notifications[0].expirationDate != null
      result.audits[0].id == createdAudit.id
   }
}
