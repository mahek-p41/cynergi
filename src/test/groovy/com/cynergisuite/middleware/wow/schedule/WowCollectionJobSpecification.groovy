package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import com.cynergisuite.middleware.wow.WowTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC

@MicronautTest(transactional = false)
class WowCollectionJobSpecification extends ServiceSpecificationBase {

   @Inject WowCollectionJob wowCollectionCustomerService
   @Inject WowTestDataLoaderService wowTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload wow collection customers" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "tstds1"}
      final wowSchedules = wowTestDataLoaderService.enableWow(tstds1)
      final scheduleEntity = wowSchedules.find {  it.title == "Wow Collections" }
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)

      when:
      def result = wowCollectionCustomerService.process(scheduleEntity, novemberMonday)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Wow Collections"
      result.rowCount() == 2383
   }
}
