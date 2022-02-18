package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure.GeneralLedgerRecurringDistributionRepository
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import jakarta.inject.Inject
import jakarta.inject.Singleton

@CompileStatic
class GeneralLedgerRecurringDistributionDataLoader {

   static Stream<GeneralLedgerRecurringDistributionEntity> stream(
      int numberIn = 1,
      GeneralLedgerRecurringEntity glRecurring,
      AccountEntity glDistributionAcct,
      Store glDistributionProfitCenter
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerRecurringDistributionEntity(
            null,
            glRecurring,
            glDistributionAcct,
            glDistributionProfitCenter,
            numbers.randomDouble(2, 1, 1000000).toBigDecimal()
         )
      }
   }

   static Stream<GeneralLedgerRecurringDistributionDTO> streamDTO(
      int numberIn = 1,
      GeneralLedgerRecurringDTO glRecurring,
      AccountDTO glDistributionAcct,
      SimpleLegacyIdentifiableDTO glDistributionProfitCenter
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerRecurringDistributionDTO([
            'generalLedgerRecurring': new SimpleIdentifiableDTO(glRecurring),
            'generalLedgerDistributionAccount': new SimpleIdentifiableDTO(glDistributionAcct),
            'generalLedgerDistributionProfitCenter': glDistributionProfitCenter,
            'generalLedgerDistributionAmount': numbers.randomDouble(2, 1, 1000000).toBigDecimal()
         ])
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class GeneralLedgerRecurringDistributionDataLoaderService {
   private final GeneralLedgerRecurringDistributionRepository generalLedgerRecurringDistributionRepository

   GeneralLedgerRecurringDistributionDataLoaderService(GeneralLedgerRecurringDistributionRepository generalLedgerRecurringDistributionRepository) {
      this.generalLedgerRecurringDistributionRepository = generalLedgerRecurringDistributionRepository
   }

   Stream<GeneralLedgerRecurringDistributionEntity> stream(
      int numberIn = 1,
      GeneralLedgerRecurringEntity glRecurring,
      AccountEntity glDistributionAcct,
      Store glDistributionProfitCenter
   ) {
      return GeneralLedgerRecurringDistributionDataLoader.stream(numberIn, glRecurring, glDistributionAcct, glDistributionProfitCenter)
         .map { generalLedgerRecurringDistributionRepository.insert(it) }
   }

   GeneralLedgerRecurringDistributionEntity single(GeneralLedgerRecurringEntity glRecurring, AccountEntity glDistributionAcct, Store glDistributionProfitCenter) {
      return stream(1, glRecurring, glDistributionAcct, glDistributionProfitCenter)
         .findFirst().orElseThrow { new Exception("Unable to create General Ledger Recurring Distribution Entity") }
   }

   GeneralLedgerRecurringDistributionDTO singleDTO(GeneralLedgerRecurringDTO glRecurring, AccountDTO glDistributionAcct, SimpleLegacyIdentifiableDTO glDistributionProfitCenter) {
      return GeneralLedgerRecurringDistributionDataLoader.streamDTO(1, glRecurring, glDistributionAcct, glDistributionProfitCenter)
         .findFirst().orElseThrow { new Exception("Unable to create General Ledger Recurring Distribution") }
   }
}
