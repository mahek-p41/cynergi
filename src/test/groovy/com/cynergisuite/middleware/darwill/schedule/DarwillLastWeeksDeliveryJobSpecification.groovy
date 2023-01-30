package com.cynergisuite.middleware.darwill.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.darwill.DarwillTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC

@MicronautTest(transactional = false)
class DarwillLastWeeksDeliveryJobSpecification extends ServiceSpecificationBase {

   @Inject DarwillLastWeeksDeliveryJob darwillLastWeeksDeliveryService
   @Inject DarwillTestDataLoaderService darwillTestDataLoaderService

   void "upload last weeks deliveries" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "coravt"}
      final darwillSchedules = darwillTestDataLoaderService.enableDarwill(tstds1)
      final scheduleEntity = darwillSchedules.find { it.title == "Darwill Last Weeks Deliveries" }
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)

      when:
      def result = darwillLastWeeksDeliveryService.process(scheduleEntity, novemberMonday)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Darwill Last Weeks Deliveries"
      result.rowCount() == 1
   }
}
