package com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringEntity
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.infrastructure.GeneralLedgerRecurringDistributionRepository
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreDTO
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerRecurringDistributionDataLoader {

   static Stream<GeneralLedgerRecurringDistributionEntity> stream(
      int numberIn = 1,
      GeneralLedgerRecurringEntity glRecurring,
      AccountEntity glDistributionAcct,
      Store glDistributionProfitCenter,
      BigDecimal amountIn = 0
   ) {
      final number = numberIn > 0 ? numberIn : 1

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerRecurringDistributionEntity(
            null,
            glRecurring.id,
            glDistributionAcct,
            glDistributionProfitCenter,
            amountIn
         )
      }
   }

   static Stream<GeneralLedgerRecurringDistributionDTO> streamDTO(
      int numberIn = 1,
      GeneralLedgerRecurringDTO glRecurring,
      AccountDTO glDistributionAcct,
      StoreDTO glDistributionProfitCenter,
      BigDecimal amountIn = 0
   ) {
      final number = numberIn > 0 ? numberIn : 1

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerRecurringDistributionDTO([
            'generalLedgerRecurring': new SimpleIdentifiableDTO(glRecurring),
            'generalLedgerDistributionAccount': glDistributionAcct,
            'generalLedgerDistributionProfitCenter': glDistributionProfitCenter,
            'generalLedgerDistributionAmount': amountIn
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
      Store glDistributionProfitCenter,
      BigDecimal amountIn = 0
   ) {
      return GeneralLedgerRecurringDistributionDataLoader.stream(numberIn, glRecurring, glDistributionAcct, glDistributionProfitCenter, amountIn)
         .map { generalLedgerRecurringDistributionRepository.insert(it) }
   }

   GeneralLedgerRecurringDistributionEntity single(
      GeneralLedgerRecurringEntity glRecurring,
      AccountEntity glDistributionAcct,
      Store glDistributionProfitCenter,
      BigDecimal amountIn = 0
   ) {
      return stream(1, glRecurring, glDistributionAcct, glDistributionProfitCenter, amountIn)
         .findFirst().orElseThrow { new Exception("Unable to create General Ledger Recurring Distribution Entity") }
   }

   GeneralLedgerRecurringDistributionDTO singleDTO(
      GeneralLedgerRecurringDTO glRecurring,
      AccountDTO glDistributionAcct,
      StoreDTO glDistributionProfitCenter,
      BigDecimal amountIn = 0
   ) {
      return GeneralLedgerRecurringDistributionDataLoader.streamDTO(1, glRecurring, glDistributionAcct, glDistributionProfitCenter, amountIn)
         .findFirst().orElseThrow { new Exception("Unable to create General Ledger Recurring Distribution") }
   }
}
