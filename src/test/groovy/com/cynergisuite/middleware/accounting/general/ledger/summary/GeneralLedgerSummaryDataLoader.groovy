package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodType
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.math.RoundingMode
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerSummaryDataLoader {

   static Stream<GeneralLedgerSummaryEntity> stream(int numberIn = 1, AccountEntity accountIn, Store profitCenterIn, OverallPeriodType periodIn) {
      def number = numberIn > 0 ? numberIn : 1
      def random = new Faker().random()
      def netActivities = new BigDecimal[12]
      for (int i = 0; i < 12; i++) {
         netActivities[i] = random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
      }
      def beginBalance = random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
      def endBalance = beginBalance.add(netActivities.sum() as BigDecimal)

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerSummaryEntity(
            null,
            accountIn,
            profitCenterIn,
            periodIn ?: OverallPeriodTypeDataLoader.random(),
            netActivities[0],
            netActivities[1],
            netActivities[2],
            netActivities[3],
            netActivities[4],
            netActivities[5],
            netActivities[6],
            netActivities[7],
            netActivities[8],
            netActivities[9],
            netActivities[10],
            netActivities[11],
            beginBalance,
            endBalance
         )
      }
   }

   static Stream<GeneralLedgerSummaryDTO> streamDTO(int numberIn = 1, SimpleIdentifiableDTO accountIn, SimpleLegacyIdentifiableDTO profitCenterIn) {
      def number = numberIn > 0 ? numberIn : 1
      def random = new Faker().random()
      def netActivities = new BigDecimal[12]
      for (int i = 0; i < 12; i++) {
         netActivities[i] = random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
      }
      def beginBalance = random.nextInt(1, 100).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
      def endBalance = beginBalance.add(netActivities.sum() as BigDecimal)

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerSummaryDTO([
            'account': accountIn,
            'profitCenter': profitCenterIn,
            'overallPeriod': new OverallPeriodTypeDTO(OverallPeriodTypeDataLoader.random()),
            'netActivityPeriod1': netActivities[0],
            'netActivityPeriod2': netActivities[1],
            'netActivityPeriod3': netActivities[2],
            'netActivityPeriod4': netActivities[3],
            'netActivityPeriod5': netActivities[4],
            'netActivityPeriod6': netActivities[5],
            'netActivityPeriod7': netActivities[6],
            'netActivityPeriod8': netActivities[7],
            'netActivityPeriod9': netActivities[8],
            'netActivityPeriod10': netActivities[9],
            'netActivityPeriod11': netActivities[10],
            'netActivityPeriod12': netActivities[11],
            'beginningBalance': beginBalance,
            'closingBalance': endBalance
         ])
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class GeneralLedgerSummaryDataLoaderService {
   private final GeneralLedgerSummaryRepository generalLedgerSummaryRepository

   GeneralLedgerSummaryDataLoaderService(GeneralLedgerSummaryRepository generalLedgerSummaryRepository) {
      this.generalLedgerSummaryRepository = generalLedgerSummaryRepository
   }

   Stream<GeneralLedgerSummaryEntity> stream(int numberIn = 1, CompanyEntity companyIn, AccountEntity accountIn, Store profitCenterIn, OverallPeriodType periodIn) {
      return GeneralLedgerSummaryDataLoader.stream(numberIn, accountIn, profitCenterIn, periodIn).map {
         generalLedgerSummaryRepository.insert(it, companyIn)
      }
   }

   GeneralLedgerSummaryEntity single(CompanyEntity companyIn, AccountEntity accountIn, Store profitCenterIn, OverallPeriodType periodIn) {
      return stream(1, companyIn, accountIn, profitCenterIn, periodIn).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerSummaryEntity") }
   }

   GeneralLedgerSummaryEntity single(CompanyEntity companyIn, AccountEntity accountIn, Store profitCenterIn) {
      return stream(1, companyIn, accountIn, profitCenterIn, null).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerSummaryEntity") }
   }


   GeneralLedgerSummaryDTO singleDTO(SimpleIdentifiableDTO accountIn, SimpleLegacyIdentifiableDTO profitCenterIn) {
      return GeneralLedgerSummaryDataLoader.streamDTO(1, accountIn, profitCenterIn).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerSummary") }
   }
}
