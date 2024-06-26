package com.cynergisuite.middleware.audit.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.audit.AuditService
import com.cynergisuite.middleware.audit.AuditTestDataLoaderService
import com.cynergisuite.middleware.audit.AuditUpdateDTO
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.audit.status.AuditStatusValueObject
import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.common.error.ValidationException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.DayOfWeek.FRIDAY
import static java.time.DayOfWeek.MONDAY
import static java.time.ZoneOffset.UTC

@MicronautTest(transactional = false)
class AuditScheduleServiceSpecification extends ServiceSpecificationBase {
   @Inject AuditService auditService
   @Inject AuditTestDataLoaderService auditFactoryService
   @Inject AuditScheduleTestDataLoaderService auditScheduleFactoryService
   @Inject AuditScheduleService auditScheduleService

   void "one store test"() {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.single(store).with { new AuthenticatedEmployee(it.id, it, store) }
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)
      final schedule = auditScheduleFactoryService.single(MONDAY, [store], employee, company, true)

      when:
      def result = auditScheduleService.process(schedule, novemberMonday)

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
      result.audits[0].store.storeNumber == store.number
   }

   void "two store test"() {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store1 = storeFactoryService.store(1, company)
      final store3 = storeFactoryService.store(3, company)
      final employee = employeeFactoryService.single(store1).with { new AuthenticatedEmployee(it.id, it, store1) }
      final novemberFriday = OffsetDateTime.of(2021, 11, 26, 0, 0, 0, 0, UTC)
      final schedule = auditScheduleFactoryService.single(FRIDAY, [store1, store3], employee, company, true)

      when:
      def result = auditScheduleService.process(schedule, novemberFriday)

      then:
      notThrown(ValidationException)
      result.notifications.size() == 2

      result.notifications[0].message == schedule.description
      result.notifications[0].sendingEmployee == employee.number.toString()
      result.notifications[0].expirationDate != null
      result.audits[0].actions.size() == 1
      result.audits[0].auditNumber > 0
      result.audits[0].currentStatus.value == "CREATED"
      result.audits[0].store.storeNumber == store1.number

      result.notifications[1].message == schedule.description
      result.notifications[1].sendingEmployee == employee.number.toString()
      result.notifications[1].expirationDate != null
      result.audits[1].actions.size() == 1
      result.audits[1].auditNumber > 0
      result.audits[1].currentStatus.value == "CREATED"
      result.audits[1].store.storeNumber == store3.number
   }

   void "one store with already CREATED audit" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store1 = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(store1)
      final createdAudit = auditFactoryService.single(store1, employee, [AuditStatusFactory.created()] as Set)
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)
      final schedule = auditScheduleFactoryService.single(MONDAY, [store1], new AuthenticatedEmployee(employee.id, employee, store1), company, true)

      when:
      def result = auditScheduleService.process(schedule, novemberMonday)

      then:
      notThrown(Exception)
      result.notifications.size() == 1
      result.audits.size() == 1

      result.notifications[0].message == schedule.description
      result.notifications[0].sendingEmployee == employee.number.toString()
      result.notifications[0].expirationDate != null
      result.notifications[0].recipients[0].description == "Audit ${createdAudit.number} is due today"
      result.audits[0].id == createdAudit.id
   }

   void "one store with already CREATED audit that is past due and is then completed" () {
      given:
      final company = companyFactoryService.forDatasetCode('coravt')
      final store1 = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(store1)
      final user = new AuthenticatedEmployee(employee.id, employee, store1)
      final createdAudit = auditFactoryService.single(store1, employee, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final schedule = auditScheduleFactoryService.single(MONDAY, [store1], new AuthenticatedEmployee(employee.id, employee, store1), company, true)
      final novemberTuesday = OffsetDateTime.of(2021, 11, 23, 0, 0, 0, 0, UTC)
      final completedStatus = new AuditStatusValueObject(AuditStatusFactory.completed())

      when: "schedule is processed on Tuesday"
      def result = auditScheduleService.process(schedule, novemberTuesday)

      then: "Past due notification should be created with the associated audit"
      notThrown(Exception)
      result.notifications.size() == 1
      result.audits.size() == 1

      result.notifications[0].message == schedule.description
      result.notifications[0].sendingEmployee == employee.number.toString()
      result.notifications[0].expirationDate != null
      result.notifications[0].recipients[0].description == "Audit ${createdAudit.number} is past due"
      result.audits[0].id == createdAudit.id

      when: "Audit is finally completed with the process running on Wednesday"
      auditService.update(new AuditUpdateDTO(createdAudit.id, completedStatus), user, Locale.getDefault())
      final novemberWednesday = OffsetDateTime.of(2021, 11, 24, 0, 0, 0, 0, UTC)
      result = auditScheduleService.process(schedule, novemberWednesday)

      then: "No notifications or audits should be created"
      notThrown(Exception)
      result.notifications.size() == 0
      result.audits.size() == 0
   }
}
