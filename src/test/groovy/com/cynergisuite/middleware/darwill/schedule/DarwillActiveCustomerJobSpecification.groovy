package com.cynergisuite.middleware.darwill.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.darwill.DarwillTestDataLoaderService
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC

@MicronautTest(transactional = false)
class DarwillActiveCustomerJobSpecification extends ServiceSpecificationBase {

   @Inject DarwillActiveCustomerJob darwillActiveCustomerService
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService

   void "upload active customers" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final schedules = darwillTestDataLoaderService.enableDarwill(tstds1)
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)
      final darwillActiveCustomerSchedule = schedules.find { it.title == "Darwill Active Customer" }

      when:
      def result = darwillActiveCustomerService.process(darwillActiveCustomerSchedule, novemberMonday)

      then:
      notThrown(Exception)
      darwillActiveCustomerService.shouldProcess(darwillActiveCustomerSchedule, novemberMonday)
      result.failureReason() == null
      result.scheduleName() == "Darwill Active Customers"
      result.rowCount() == 3260
   }
}
