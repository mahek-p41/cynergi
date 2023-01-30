package com.cynergisuite.middleware.darwill.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.darwill.DarwillTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC

@MicronautTest(transactional = false)
class DarwillBirthdayJobSpecification extends ServiceSpecificationBase {

   @Inject DarwillBirthdayJob darwillBirthdayService
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService

   void "upload birthdays" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "coravt"}
      final darwillSchedules = darwillTestDataLoaderService.enableDarwill(tstds1)
      final scheduleEntity = darwillSchedules.find { it.title == "Darwill Birthdays" }
      final novemberFirst = OffsetDateTime.of(2021, 11, 1, 0, 0, 0, 0, UTC)

      when:
      def result = darwillBirthdayService.process(scheduleEntity, novemberFirst)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Darwill Birthdays"
      result.rowCount() == 109
   }
}
