package com.cynergisuite.middleware.darwill

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import com.cynergisuite.middleware.schedule.command.DarwillLastWeeksDelivery
import com.cynergisuite.middleware.schedule.type.Weekly
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import java.time.DayOfWeek
import jakarta.inject.Inject

@MicronautTest(transactional = false)
class DarwillLastWeeksDeliveryServiceSpecification extends ServiceSpecificationBase {

   @Inject DarwillLastWeeksDeliveryService darwillLastWeeksDeliveryService
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload last weeks deliveries" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final darwillSchedules = darwillTestDataLoaderService.enableDarwill(tstds1)
      final scheduleEntity = darwillSchedules.find { it.title == "Darwill Last Weeks Deliveries" }

      when:
      def result = darwillLastWeeksDeliveryService.process(scheduleEntity, DayOfWeek.SUNDAY)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Darwill Last Weeks Deliveries"
      result.rowCount() == 5
   }
}
