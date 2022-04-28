package com.cynergisuite.middleware.accounting.general.ledger.reversal.entry

import com.cynergisuite.middleware.accounting.general.ledger.reversal.GeneralLedgerReversalDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.distribution.GeneralLedgerReversalDistributionDTO
import com.cynergisuite.middleware.accounting.general.ledger.reversal.entry.infrastructure.GeneralLedgerReversalEntryRepository
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerReversalEntryDataLoader {

   static Stream<GeneralLedgerReversalEntryDTO> streamDTO(
      int numberIn = 1,
      GeneralLedgerReversalDTO glReversal,
      List<GeneralLedgerReversalDistributionDTO> glReversalDistributions
   ) {
      final number = numberIn > 0 ? numberIn : 1

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerReversalEntryDTO(
            glReversal,
            glReversalDistributions,
            BigDecimal.ZERO
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class GeneralLedgerReversalEntryDataLoaderService {
   private final GeneralLedgerReversalEntryRepository generalLedgerReversalEntryRepository

   GeneralLedgerReversalEntryDataLoaderService(GeneralLedgerReversalEntryRepository generalLedgerReversalEntryRepository) {
      this.generalLedgerReversalEntryRepository = generalLedgerReversalEntryRepository
   }

   static GeneralLedgerReversalEntryDTO singleDTO(
      GeneralLedgerReversalDTO glReversal,
      List<GeneralLedgerReversalDistributionDTO> glReversalDistributions
   ) {
      return GeneralLedgerReversalEntryDataLoader.streamDTO(1, glReversal, glReversalDistributions)
         .findFirst().orElseThrow { new Exception("Unable to create General Ledger Reversal Entry DTO") }
   }
}
