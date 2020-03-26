package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.audit.status.Created
import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject

import static java.time.DayOfWeek.TUESDAY
import static java.time.DayOfWeek.WEDNESDAY

@MicronautTest(transactional = false)
class ScheduleServiceSpecification extends ServiceSpecificationBase {
   @Inject AuditRepository auditRepository
   @Inject AuditScheduleFactoryService auditScheduleFactoryService
   @Inject EmployeeFactoryService employeeFactoryService
   @Inject ScheduleService scheduleService

   void "execute daily Tuesday audit job on Tuesday" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(storeOne)
      final user = new AuthenticatedUser(employee.id, employee.type, employee.number, company, employee.department, storeOne, "A", 0, false) // make ourselves a user who can see all audits
      final tuesdaySchedule = auditScheduleFactoryService.single(TUESDAY, [storeOne], user, company)

      when:
      def result = scheduleService.runDaily(TUESDAY)
      def audit = auditRepository.findOneCreatedOrInProgress(storeOne)
      def audits = auditRepository.findAll(new AuditPageRequest(null), user)

      then:
      notThrown(Exception)
      result == 1
      audit != null
      audit.store.myNumber() == storeOne.myNumber()
      audit.store.myCompany() == storeOne.myCompany()
      audit.actions.size() == 1
      audit.actions[0].status.value == Created.INSTANCE.value
      audits.elements.size() == 1
   }

   void "execute daily Tuesday audit job on Wednesday" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(storeOne)
      final user = new AuthenticatedUser(employee.id, employee.type, employee.number, company, employee.department, storeOne, "A", 0, false) // make ourselves a user who can see all audits
      final tuesdaySchedule = auditScheduleFactoryService.single(TUESDAY, [storeOne], user, company)

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
