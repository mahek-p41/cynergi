package com.cynergisuite.middleware.darwill

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import com.cynergisuite.middleware.schedule.command.DarwillInactiveCustomer
import com.cynergisuite.middleware.schedule.type.Weekly
import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import jakarta.inject.Inject
import java.time.DayOfWeek

@MicronautTest(transactional = false)
class DarwillInactiveCustomerServiceSpecification extends ServiceSpecificationBase {

   @Inject DarwillInactiveCustomerService darwillInactiveCustomerService
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload inactive customers" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final darwillSchedules = darwillTestDataLoaderService.enableDarwill(tstds1)
      final scheduleEntity = darwillSchedules.find { it.title == "Darwill Inactive Customer" }

      when:
      def result = darwillInactiveCustomerService.process(scheduleEntity, DayOfWeek.SUNDAY)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Darwill Inactive Customers"
      result.rowCount() == 2399
   }
}
