package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import com.cynergisuite.middleware.wow.WowTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC


@MicronautTest(transactional = false)
class WowAccountSummaryJobSpecification extends ServiceSpecificationBase{

   @Inject WowAccountSummaryJob wowAccountSumaryService
   @Inject WowTestDataLoaderService wowTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload account summary" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final Schedules = wowTestDataLoaderService.enableWow(tstds1)
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)
      final wowAccountSummarySchedule = Schedules.find { it.title == "Wow Account Summary"}

      when:
      def result = wowAccountSumaryService.process(wowAccountSummarySchedule, novemberMonday)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Wow Account Summary"
      result.rowCount() == 2383
   }
}
