package com.cynergisuite.middleware.wow.schedule

import com.cynergisuite.domain.infrastructure.ServiceSpecificationBase
import com.cynergisuite.middleware.schedule.ScheduleTestDataLoaderService
import com.cynergisuite.middleware.wow.WowTestDataLoaderService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

import java.time.OffsetDateTime

import static java.time.ZoneOffset.UTC


@MicronautTest(transactional = false)
class WowAllRtoAgreementsJobSpecification extends ServiceSpecificationBase{

   @Inject WowAllRtoAgreementJob wowAllRtoAgreementService
   @Inject WowTestDataLoaderService wowTestDataLoaderService
   @Inject ScheduleTestDataLoaderService scheduleTestDataLoaderService

   void "upload all rto agreements" () {
      given:
      final tstds1 = companies.find { it.datasetCode == "coravt"}
      final Schedules = wowTestDataLoaderService.enableWow(tstds1)
      final novemberMonday = OffsetDateTime.of(2021, 11, 29, 0, 0, 0, 0, UTC)
      final wowAllRtoAgreementSchedule = Schedules.find { it.title == "Wow All Rto Agreements"}

      when:
      def result = wowAllRtoAgreementService.process(wowAllRtoAgreementSchedule, novemberMonday)

      then:
      notThrown(Exception)
      result.failureReason() == null
      result.scheduleName() == "Wow All Rto Agreements"
      result.rowCount() == 9727
   }
}
