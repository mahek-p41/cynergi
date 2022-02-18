package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries

import com.cynergisuite.middleware.accounting.general.ledger.recurring.GeneralLedgerRecurringDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.distribution.GeneralLedgerRecurringDistributionDTO
import com.cynergisuite.middleware.accounting.general.ledger.recurring.entries.infrastructure.GeneralLedgerRecurringEntriesRepository
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import jakarta.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerRecurringEntriesDataLoader {

   static Stream<GeneralLedgerRecurringEntriesDTO> streamDTO(
      int numberIn = 1,
      GeneralLedgerRecurringDTO glRecurring,
      List<GeneralLedgerRecurringDistributionDTO> glRecurringDistributions
   ) {
      final number = numberIn > 0 ? numberIn : 1

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerRecurringEntriesDTO(
            glRecurring,
            glRecurringDistributions,
            BigDecimal.ZERO
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class GeneralLedgerRecurringEntriesDataLoaderService {
   private final GeneralLedgerRecurringEntriesRepository generalLedgerRecurringEntriesRepository

   GeneralLedgerRecurringEntriesDataLoaderService(GeneralLedgerRecurringEntriesRepository generalLedgerRecurringEntriesRepository) {
      this.generalLedgerRecurringEntriesRepository = generalLedgerRecurringEntriesRepository
   }

   static GeneralLedgerRecurringEntriesDTO singleDTO(
      GeneralLedgerRecurringDTO glRecurring,
      List<GeneralLedgerRecurringDistributionDTO> glRecurringDistributions
   ) {
      return GeneralLedgerRecurringEntriesDataLoader.streamDTO(1, glRecurring, glRecurringDistributions)
         .findFirst().orElseThrow { new Exception("Unable to create General Ledger Recurring Entries DTO") }
   }
}
