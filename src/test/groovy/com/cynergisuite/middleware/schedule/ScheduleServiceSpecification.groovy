package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.audit.status.Created
import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import com.cynergisuite.middleware.employee.EmployeeFactoryService
import com.cynergisuite.middleware.store.StoreFactoryService
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
   @Inject StoreFactoryService storeFactoryService

   void "execute daily Tuesday audit job on Tuesday" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(storeOne)
      final tuesdaySchedule = auditScheduleFactoryService.single(TUESDAY, [storeOne], new AuthenticatedUser(employee, storeOne), company)

      when:
      def result = scheduleService.runDaily(TUESDAY)
      //TODO Does the above in given create an AuthenticatedUser I can use here?
      def audit = auditRepository.findOneCreatedOrInProgress(storeOne)
      def audits = auditRepository.findAll(new AuditPageRequest(null), company)

      then:
      notThrown(Exception)
      result == 1
      audit != null
      audit.store == storeOne
      audit.actions.size() == 1
      audit.actions[0].status.value == Created.INSTANCE.value
      audits.elements.size() == 1
   }

   void "execute daily Tuesday audit job on Wednesday" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(storeOne)
      final tuesdaySchedule = auditScheduleFactoryService.single(TUESDAY, [storeOne], new AuthenticatedUser(employee, storeOne), company)

      when:
      def result = scheduleService.runDaily(WEDNESDAY)
      //TODO Does the above in given create an AuthenticatedUser I can use here?
      def audit = auditRepository.findOneCreatedOrInProgress(storeOne)
      def audits = auditRepository.findAll(new AuditPageRequest(null), company)

      then:
      notThrown(Exception)
      result == 0
      audit == null
      audits.elements.size() == 0
   }
}
