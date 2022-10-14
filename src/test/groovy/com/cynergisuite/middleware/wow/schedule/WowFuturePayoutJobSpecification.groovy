package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import com.cynergisuite.middleware.wow.WowTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC

@MicronautTest(transactional = false)
class WowFuturePayoutJobSpecification extends ServiceSpecificationBase {

   @Inject WowFuturePayoutJob wowFuturePayoutService
   @Inject WowTestDataLoaderService wowTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload future payout customer" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final wowSchedules = wowTestDataLoaderService.enableWow(tstds1)
      final scheduleEntity = wowSchedules.find { it.title == "Wow Future Payouts Next 30 Days" }
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)



      when:
      def result = wowFuturePayoutService.process(scheduleEntity, novemberMonday)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Wow Future Payouts Next 30 Days"
      result.rowCount() == 1
   }
}
