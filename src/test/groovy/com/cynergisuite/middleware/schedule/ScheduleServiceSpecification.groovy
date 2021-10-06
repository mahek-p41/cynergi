package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.audit.AuditTestDataLoaderService
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.schedule.AuditScheduleTestDataLoaderService
import com.cynergisuite.middleware.audit.status.AuditStatusFactory
import com.cynergisuite.middleware.authentication.user.AuthenticatedUser
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

import static java.time.DayOfWeek.TUESDAY
import static java.time.DayOfWeek.WEDNESDAY

@MicronautTest(transactional = false)
class ScheduleServiceSpecification extends ServiceSpecificationBase {
   @Inject AuditRepository auditRepository
   @Inject AuditScheduleTestDataLoaderService auditScheduleTestDataLoaderService
   @Inject AuditTestDataLoaderService auditTestDataLoaderService
   @Inject ScheduleJobExecutorService scheduleService

   void "execute daily Tuesday audit job on Tuesday dataset 1" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      auditTestDataLoaderService.single(storeOne, [AuditStatusFactory.created()] as Set)
      auditTestDataLoaderService.single(storeOne, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      auditTestDataLoaderService.single(storeOne, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final employee = employeeFactoryService.single(storeOne)
      final user = new AuthenticatedUser(employee.id, employee.type, employee.number, company, employee.department, storeOne, "A", 0, false) // make ourselves a user who can see all audits

      auditScheduleTestDataLoaderService.single(TUESDAY, [storeOne], user, company)

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
      auditTestDataLoaderService.single(storeOne, [AuditStatusFactory.created()] as Set)
      auditTestDataLoaderService.single(storeOne, [AuditStatusFactory.created(), AuditStatusFactory.inProgress(), AuditStatusFactory.completed()] as Set)
      final employee = employeeFactoryService.single(storeOne)
      final user = new AuthenticatedUser(employee.id, employee.type, employee.number, company, employee.department, storeOne, "A", 0, false) // make ourselves a user who can see all audits

      auditScheduleTestDataLoaderService.single(TUESDAY, [storeOne], user, company)

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

   void "execute daily Tuesday audit job on Wednesday" () {
      given:
      final company = companyFactoryService.forDatasetCode('tstds1')
      final storeOne = storeFactoryService.store(1, company)
      final employee = employeeFactoryService.single(storeOne)
      auditTestDataLoaderService.single(storeOne, [AuditStatusFactory.created(), AuditStatusFactory.inProgress()] as Set)
      final user = new AuthenticatedUser(employee.id, employee.type, employee.number, company, employee.department, storeOne, "A", 0, false) // make ourselves a user who can see all audits
      auditScheduleTestDataLoaderService.single(TUESDAY, [storeOne], user, company)

      when:
      def result = scheduleService.runDaily(WEDNESDAY)
      def audit = auditRepository.findOneCreatedOrInProgress(storeOne)

      then:
      notThrown(Exception)
      result == 1
      audit != null
      audit.store.myNumber() == storeOne.myNumber()
      audit.store.myCompany() == storeOne.myCompany()
      audit.actions.size() == 2
      audit.currentStatus() == AuditStatusFactory.inProgress()
   }
}
