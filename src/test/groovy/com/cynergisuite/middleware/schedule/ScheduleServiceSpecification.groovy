package com.cynergisuite.middleware.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.audit.infrastructure.AuditPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditRepository
import com.cynergisuite.middleware.audit.schedule.AuditScheduleFactoryService
import com.cynergisuite.middleware.audit.status.Created
import com.cynergisuite.middleware.store.StoreFactory
import io.micronaut.test.annotation.MicronautTest
import javax.inject.Inject

import static java.time.DayOfWeek.TUESDAY
import static java.time.DayOfWeek.WEDNESDAY

@MicronautTest(transactional = false)
class ScheduleServiceSpecification extends ServiceSpecificationBase {
   @Inject AuditRepository auditRepository
   @Inject AuditScheduleFactoryService auditScheduleFactoryService
   @Inject ScheduleService scheduleService

   void "execute daily Tuesday audit job on Tuesday" () {
      given:
      final storeOne = StoreFactory.storeOne()
      final tuesdaySchedule = auditScheduleFactoryService.single(TUESDAY, [storeOne], null, "tstds1")

      when:
      def result = scheduleService.runDaily(TUESDAY)
      def audit = auditRepository.findOneCreatedOrInProgress(storeOne)
      def audits = auditRepository.findAll(new AuditPageRequest(null), "tstds1")

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
      final storeOne = StoreFactory.storeOne()
      final tuesdaySchedule = auditScheduleFactoryService.single(TUESDAY, [storeOne], null, "tstds1")

      when:
      def result = scheduleService.runDaily(WEDNESDAY)
      def audit = auditRepository.findOneCreatedOrInProgress(storeOne)
      def audits = auditRepository.findAll(new AuditPageRequest(null), "tstds1")

      then:
      notThrown(Exception)
      result == 0
      audit == null
      audits.elements.size() == 0
   }
}
