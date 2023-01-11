package com.cynergisuite.middleware.darwill.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.darwill.DarwillTestDataLoaderService
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC

@MicronautTest(transactional = false)
class DarwillLastWeeksPayoutJobSpecification extends ServiceSpecificationBase{

   @Inject DarwillLastWeeksPayoutJob darwillLastWeeksPayoutService
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService

   void "upload last weeks payouts" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final darwillSchedules = darwillTestDataLoaderService.enableDarwill(tstds1)
      final scheduleEntity = darwillSchedules.find { it.title == "Darwill Last Weeks Payouts"}
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)

      when:
      def result = darwillLastWeeksPayoutService.process(scheduleEntity, novemberMonday)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Darwill Last Weeks Payouts"
      result.rowCount() == 5
   }
}
