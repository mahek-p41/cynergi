package com.cynergisuite.middleware.darwill

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject


import static java.time.DayOfWeek.MONDAY

@MicronautTest(transactional = false)
class DarwillActiveCustomerServiceSpecification extends ServiceSpecificationBase {

   @Inject DarwillActiveCustomerService darwillActiveCustomerService
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload active customers" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final schedules = darwillTestDataLoaderService.enableDarwill(tstds1)

      when:
      def result = darwillActiveCustomerService.process(schedules.find { it.title == "Darwill Active Customer" }, MONDAY)

      then:
      notThrown(Exception)
      darwillActiveCustomerService.shouldProcess(MONDAY)
      result.failureReason() == null
      result.scheduleName() == "Darwill Active Customers"
      result.rowCount() == 3260
   }
}
