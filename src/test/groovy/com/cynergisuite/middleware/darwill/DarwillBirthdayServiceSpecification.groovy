package com.cynergisuite.middleware.darwill

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import java.time.LocalDate
import jakarta.inject.Inject


import static java.time.Month.NOVEMBER

@MicronautTest(transactional = false)
class DarwillBirthdayServiceSpecification extends ServiceSpecificationBase {

   @Inject DarwillBirthdayService darwillBirthdayService
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload birthdays" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final darwillSchedules = darwillTestDataLoaderService.enableDarwill(tstds1)
      final scheduleEntity = darwillSchedules.find { it.title == "Darwill Birthdays" }

      final getNovember = LocalDate.of(2021, NOVEMBER, 28).getMonth()
      final testDecember = getNovember + 1L

      when:
      def result = darwillBirthdayService.process(scheduleEntity, testDecember)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Darwill Birthdays"
      result.rowCount() == 165
   }
}
