package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.audit.AuditFactoryService
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.schedule.command.ScheduleCommandTypeFactory
import com.cynergisuite.middleware.schedule.type.Daily
import com.cynergisuite.middleware.schedule.type.Weekly
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject
import java.time.OffsetDateTime

import static java.time.DayOfWeek.TUESDAY
import static java.time.DayOfWeek.WEDNESDAY

@MicronautTest(transactional = false)
class ScheduleServiceSpecification extends ServiceSpecificationBase {
   @Inject AuditRepository auditRepository
   @Inject AuditScheduleFactoryService auditScheduleFactoryService
   @Inject EmployeeFactoryService employeeFactoryService
   @Inject AuditFactoryService auditFactoryService
   @Inject ScheduleService scheduleService

   void "execute daily Tuesday audit job on Tuesday dataset 1" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      auditFactoryService.single(storeOne, [AuditStatusFactory.created()] as Set)
      auditFactoryService.single(storeOne, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      auditFactoryService.single(storeOne, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set, OffsetDateTime.now().minusDays(10))
      final employee = employeeFactoryService.single(storeOne)
      final user = new AuthenticatedUser(employee.id, employee.type, employee.number, company, employee.department, storeOne, "A", 0, false) // make ourselves a user who can see all audits

      auditScheduleFactoryService.single(ScheduleCommandTypeFactory.INSTANCE.auditSchedule(), Weekly.INSTANCE, TUESDAY, [storeOne], user, company)

      when:
      def result = scheduleService.runDaily(TUESDAY)
      def audit = auditRepository.findOneCreatedOrInProgress(storeOne)

      then:
      notThrown(Exception)
      result == 1
      audit != null
      audit.store.myNumber() == storeOne.myNumber()
      audit.store.myCompany() == storeOne.myCompany()
      audit.actions.size() <= 2
      [AuditStatusFactory.created(), AuditStatusFactory.inProgress()].contains(audit.currentStatus())
   }

   void "execute daily Tuesday audit job on Tuesday dataset 2" () {
      given: 'One past due audit in status open'
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      auditFactoryService.single(storeOne, [AuditStatusFactory.created()] as Set, OffsetDateTime.now().minusDays(2))
      auditFactoryService.single(storeOne, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set, OffsetDateTime.now().minusDays(10))
      final employee = employeeFactoryService.single(storeOne)
      final user = new AuthenticatedUser(employee.id, employee.type, employee.number, company, employee.department, storeOne, "A", 0, false) // make ourselves a user who can see all audits

      auditScheduleFactoryService.single(ScheduleCommandTypeFactory.INSTANCE.auditSchedule(), Weekly.INSTANCE, TUESDAY, [storeOne], user, company)

      when:
      def result = scheduleService.runDaily(TUESDAY)
      def audit = auditRepository.findOneCreatedOrInProgress(storeOne)

      then:
      notThrown(Exception)
      result == 1
      audit != null
      audit.store.myNumber() == storeOne.myNumber()
      audit.store.myCompany() == storeOne.myCompany()
      audit.actions.size() <= 2
      [AuditStatusFactory.created(), AuditStatusFactory.inProgress()].contains(audit.currentStatus())
   }

   void "execute daily job to send notification for past due audits" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final storeThree = storeFactoryService.store(3, company)
      auditFactoryService.single(storeOne, [AuditStatusFactory.created()] as Set, OffsetDateTime.now().minusDays(2))
      auditFactoryService.single(storeOne, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set, OffsetDateTime.now().minusDays(10))
      auditFactoryService.single(storeOne, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set, OffsetDateTime.now().minusDays(10))
      final employee = employeeFactoryService.single(storeOne)
      final user = new AuthenticatedUser(employee.id, employee.type, employee.number, company, employee.department, storeOne, "A", 0, false)
      auditScheduleFactoryService.single(ScheduleCommandTypeFactory.INSTANCE.pastDueAuditReminder(), Daily.INSTANCE, [storeOne, storeThree], user, company)

      when:
      def result = scheduleService.runDaily()
      def audits = auditRepository.findAllPastDue(storeOne)

      then:
      notThrown(Exception)
      result == 1
      audits.size() == 2
   }

   void "execute daily Tuesday audit job on Wednesday" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(storeOne)
      final user = new AuthenticatedUser(employee.id, employee.type, employee.number, company, employee.department, storeOne, "A", 0, false) // make ourselves a user who can see all audits
      final tuesdaySchedule = auditScheduleFactoryService.single(ScheduleCommandTypeFactory.INSTANCE.auditSchedule(), Weekly.INSTANCE, [storeOne], user, company)

      when:
      def result = scheduleService.runDaily(WEDNESDAY)
      def audit = auditRepository.findOneCreatedOrInProgress(storeOne)
      def audits = auditRepository.findAll(new AuditPageRequest(null), user)

      then:
      notThrown(Exception)
      result == 0
      audit == null
      audits.elements.size() == 0
   }
}
