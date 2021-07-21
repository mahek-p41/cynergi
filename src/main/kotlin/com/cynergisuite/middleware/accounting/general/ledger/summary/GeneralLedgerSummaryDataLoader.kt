package com.cynergisuite.middleware.accounting.general.ledger.summary

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.summary.infrastructure.GeneralLedgerSummaryRepository
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodTypeDTO
import com.cynergisuite.middleware.accounting.routine.type.OverallPeriodTypeDataLoader
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.Store
import io.micronaut.context.annotation.Requires
import java.math.RoundingMode
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

object GeneralLedgerSummaryDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, accountIn: AccountEntity, profitCenterIn: Store): Stream<GeneralLedgerSummaryEntity> {
      val number = if (numberIn > 0) numberIn else 1

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerSummaryEntity(
            account = accountIn,
            profitCenter = profitCenterIn,
            overallPeriod = OverallPeriodTypeDataLoader.random(),
            netActivityPeriod1 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod2 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod3 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod4 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod5 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod6 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod7 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod8 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod9 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod10 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod11 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod12 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            beginningBalance = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            closingBalance = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, accountIn: SimpleIdentifiableDTO, profitCenterIn: SimpleLegacyIdentifiableDTO): Stream<GeneralLedgerSummaryDTO> {
      val number = if (numberIn < 0) 1 else numberIn

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerSummaryDTO(
            id = null,
            account = accountIn,
            profitCenter = profitCenterIn,
            overallPeriod = OverallPeriodTypeDTO(OverallPeriodTypeDataLoader.random()),
            netActivityPeriod1 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod2 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod3 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod4 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod5 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod6 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod7 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod8 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod9 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod10 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod11 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            netActivityPeriod12 = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            beginningBalance = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN),
            closingBalance = Random.nextInt().toBigDecimal().setScale(2, RoundingMode.HALF_EVEN)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerSummaryDataLoaderService @Inject constructor(
   private val generalLedgerSummaryRepository: GeneralLedgerSummaryRepository
) {
   fun stream(numberIn: Int = 1, companyIn: Company, accountIn: AccountEntity, profitCenterIn: Store): Stream<GeneralLedgerSummaryEntity> {
      return GeneralLedgerSummaryDataLoader.stream(numberIn, accountIn, profitCenterIn).map {
         generalLedgerSummaryRepository.insert(it, companyIn)
      }
   }

   fun single(companyIn: Company, accountIn: AccountEntity, profitCenterIn: Store): GeneralLedgerSummaryEntity {
      return stream(1, companyIn, accountIn, profitCenterIn).findFirst().orElseThrow { Exception("Unable to create GeneralLedgerSummaryEntity") }
   }

   fun singleDTO(accountIn: SimpleIdentifiableDTO, profitCenterIn: SimpleLegacyIdentifiableDTO): GeneralLedgerSummaryDTO {
      return GeneralLedgerSummaryDataLoader.streamDTO(1, accountIn, profitCenterIn).findFirst().orElseThrow { Exception("Unable to create GeneralLedgerSummary") }
   }
}
