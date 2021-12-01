package com.cynergisuite.middleware.darwill

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import java.time.DayOfWeek
import jakarta.inject.Inject

@MicronautTest(transactional = false)
class DarwillCollectionServiceSpecification extends ServiceSpecificationBase {

   @Inject DarwillCollectionService darwillCollectionCustomerService
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload collection customers" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final darwillSchedules = darwillTestDataLoaderService.enableDarwill(tstds1)
      final scheduleEntity = darwillSchedules.find {  it.title == "Darwill Collections" }

      when:
      def result = darwillCollectionCustomerService.process(scheduleEntity, DayOfWeek.SUNDAY)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Darwill Collections"
      result.rowCount() == 3260
   }
}
