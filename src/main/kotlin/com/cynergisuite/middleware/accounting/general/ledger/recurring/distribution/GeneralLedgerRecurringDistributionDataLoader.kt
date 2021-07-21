package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableEntity
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure.GeneralLedgerRecurringDistributionRepository
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object GeneralLedgerRecurringDistributionDataLoader {

   @JvmStatic
   fun stream(
      numberIn: Int = 1,
      glRecurring: GeneralLedgerRecurringEntity,
      glDistributionAcct: AccountEntity,
      glDistributionProfitCenter: Store
   ): Stream<GeneralLedgerRecurringDistributionEntity> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerRecurringDistributionEntity(
            generalLedgerRecurring = glRecurring,
            generalLedgerDistributionAccount = glDistributionAcct,
            generalLedgerDistributionProfitCenter = SimpleLegacyIdentifiableEntity(glDistributionProfitCenter.myId()),
            generalLedgerDistributionAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal()
         )
      }
   }

   @JvmStatic
   fun streamDTO(
      numberIn: Int = 1,
      glRecurring: GeneralLedgerRecurringDTO,
      glDistributionAcct: SimpleIdentifiableDTO,
      glDistributionProfitCenter: SimpleLegacyIdentifiableDTO
   ): Stream<GeneralLedgerRecurringDistributionDTO> {
      val number = if (numberIn < 0) 1 else numberIn
      val faker = Faker()
      val numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         GeneralLedgerRecurringDistributionDTO(
            generalLedgerRecurring = SimpleIdentifiableDTO(glRecurring),
            generalLedgerDistributionAccount = glDistributionAcct,
            generalLedgerDistributionProfitCenter = glDistributionProfitCenter,
            generalLedgerDistributionAmount = numbers.randomDouble(2, 1, 1000000).toBigDecimal()
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerRecurringDistributionDataLoaderService @Inject constructor(
   private val generalLedgerRecurringDistributionRepository: GeneralLedgerRecurringDistributionRepository
) {

   fun stream(
      numberIn: Int = 1,
      glRecurring: GeneralLedgerRecurringEntity,
      glDistributionAcct: AccountEntity,
      glDistributionProfitCenter: Store
   ): Stream<GeneralLedgerRecurringDistributionEntity> {
      return GeneralLedgerRecurringDistributionDataLoader.stream(numberIn, glRecurring, glDistributionAcct, glDistributionProfitCenter)
         .map { generalLedgerRecurringDistributionRepository.insert(it) }
   }

   fun single(glRecurring: GeneralLedgerRecurringEntity, glDistributionAcct: AccountEntity, glDistributionProfitCenter: Store): GeneralLedgerRecurringDistributionEntity {
      return stream(1, glRecurring, glDistributionAcct, glDistributionProfitCenter)
         .findFirst().orElseThrow { Exception("Unable to create General Ledger Recurring Distribution Entity") }
   }

   fun singleDTO(glRecurring: GeneralLedgerRecurringDTO, glDistributionAcct: SimpleIdentifiableDTO, glDistributionProfitCenter: SimpleLegacyIdentifiableDTO): GeneralLedgerRecurringDistributionDTO {
      return GeneralLedgerRecurringDistributionDataLoader.streamDTO(1, glRecurring, glDistributionAcct, glDistributionProfitCenter)
         .findFirst().orElseThrow { Exception("Unable to create General Ledger Recurring Distribution") }
   }
}
