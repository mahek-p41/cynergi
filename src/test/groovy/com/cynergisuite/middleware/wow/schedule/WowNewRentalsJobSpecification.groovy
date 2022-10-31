package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import com.cynergisuite.middleware.wow.WowTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC


@MicronautTest(transactional = false)
class WowNewRentalsJobSpecification extends ServiceSpecificationBase{

   @Inject WowNewRentalJob wowNewRentalService
   @Inject WowTestDataLoaderService wowTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload new rentals" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final Schedules = wowTestDataLoaderService.enableWow(tstds1)
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)
      final wowNewRentalSchedule = Schedules.find { it.title == "Wow New Rentals Last 30 Days"}

      when:
      def result = wowNewRentalService.process(wowNewRentalSchedule, novemberMonday)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Wow New Rentals Last 30 Days"
      result.rowCount() == 1
   }
}

