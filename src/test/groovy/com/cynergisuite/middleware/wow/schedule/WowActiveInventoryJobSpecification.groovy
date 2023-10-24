package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.wow.WowTestDataLoaderService
import com.cynergisuite.middleware.wow.schedule.WowActiveInventoryJob
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC
@MicronautTest(transactional = false)
class WowActiveInventoryJobSpecification extends ServiceSpecificationBase {

   @Inject WowActiveInventoryJob wowActiveInventoryService
   @Inject WowTestDataLoaderService wowTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload active inventory" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "coravt"}
      final schedules = wowTestDataLoaderService.enableWow(tstds1)
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)
      final wowActiveInventorySchedule = schedules.find { it.title == "Wow Active Inventory" }

      when:
      def result = wowActiveInventoryService.process(wowActiveInventorySchedule,novemberMonday)

      then:
      notThrown(Exception)
      wowActiveInventoryService.shouldProcess(wowActiveInventorySchedule, novemberMonday)
      result.failureReason() == null
      result.scheduleName() == "Wow Active Inventory"
      result.rowCount() == 1354
   }
}
