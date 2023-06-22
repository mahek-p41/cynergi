package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import com.cynergisuite.middleware.wow.WowTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC

@MicronautTest(transactional = false)
class WowReturnJobSpecification extends ServiceSpecificationBase {

   @Inject WowReturnJob wowReturnService
   @Inject WowTestDataLoaderService wowTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload return customer" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "coravt"}
      final wowSchedules = wowTestDataLoaderService.enableWow(tstds1)
      final scheduleEntity = wowSchedules.find { it.title == "Wow Returns Last 120 Days" }
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)



      when:
      def result = wowReturnService.process(scheduleEntity, novemberMonday)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Wow Returns Last 120 Days"
      result.rowCount() == 4776
   }
}
