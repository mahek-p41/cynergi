package com.cynergisuite.middleware.darwill

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import static java.time.DayOfWeek.SUNDAY

@MicronautTest(transactional = false)
class DarwillLastWeeksPayoutServiceSpecification extends ServiceSpecificationBase{

   @Inject DarwillLastWeeksPayoutService darwillLastWeeksPayoutService
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload last weeks payouts" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final darwillSchedules = darwillTestDataLoaderService.enableDarwill(tstds1)
      final scheduleEntity = darwillSchedules.find { it.title == "Darwill Last Weeks Payouts"}

      when:
      def result = darwillLastWeeksPayoutService.process(scheduleEntity, SUNDAY)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Darwill Last Weeks Payouts"
      result.rowCount() == 5
   }
}
