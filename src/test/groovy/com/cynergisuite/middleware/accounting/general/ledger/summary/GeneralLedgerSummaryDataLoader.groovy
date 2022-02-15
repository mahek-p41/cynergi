package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.accounting.financial.calendar.type.OverallPeriodTypeDTO
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

   static Stream<GeneralLedgerSummaryEntity> stream(int numberIn = 1, AccountEntity accountIn, Store profitCenterIn) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerSummaryEntity(
            null,
            accountIn,
            profitCenterIn,
            OverallPeriodTypeDataLoader.random(),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
         )
      }
   }

   static Stream<GeneralLedgerSummaryDTO> streamDTO(int numberIn = 1, SimpleIdentifiableDTO accountIn, SimpleLegacyIdentifiableDTO profitCenterIn) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final random = faker.random()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerSummaryDTO([
            'account': accountIn,
            'profitCenter': profitCenterIn,
            'overallPeriod': new OverallPeriodTypeDTO(OverallPeriodTypeDataLoader.random()),
            'netActivityPeriod1': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod2': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod3': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod4': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod5': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod6': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod7': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod8': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod9': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod10': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod11': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'netActivityPeriod12': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'beginningBalance': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            'closingBalance': random.nextInt(1, Integer.MAX_VALUE).toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
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

   Stream<GeneralLedgerSummaryEntity> stream(int numberIn = 1, CompanyEntity companyIn, AccountEntity accountIn, Store profitCenterIn) {
      return GeneralLedgerSummaryDataLoader.stream(numberIn, accountIn, profitCenterIn).map {
         generalLedgerSummaryRepository.insert(it, companyIn)
      }
   }

   GeneralLedgerSummaryEntity single(CompanyEntity companyIn, AccountEntity accountIn, Store profitCenterIn) {
      return stream(1, companyIn, accountIn, profitCenterIn).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerSummaryEntity") }
   }

   GeneralLedgerSummaryDTO singleDTO(SimpleIdentifiableDTO accountIn, SimpleLegacyIdentifiableDTO profitCenterIn) {
      return GeneralLedgerSummaryDataLoader.streamDTO(1, accountIn, profitCenterIn).findFirst().orElseThrow { new Exception("Unable to create GeneralLedgerSummary") }
   }
}
