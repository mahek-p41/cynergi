package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.wow.schedule.WowBirthdayJob
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import com.cynergisuite.middleware.wow.WowTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC

@MicronautTest(transactional = false)
class WowBirthdayJobSpecification extends ServiceSpecificationBase {

   @Inject WowBirthdayJob wowBirthdayService
   @Inject WowTestDataLoaderService wowTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload wow birthdays" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "coravt"}
      final wowSchedules = wowTestDataLoaderService.enableWow(tstds1)
      final scheduleEntity = wowSchedules.find { it.title == "Wow Birthdays" }
      final novemberFirst = OffsetDateTime.of(2021, 11, 1, 0, 0, 0, 0, UTC)

      when:
      def result = wowBirthdayService.process(scheduleEntity, novemberFirst)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Wow Birthdays"
      result.rowCount() == 6
   }
}
