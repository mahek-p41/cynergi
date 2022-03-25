package com.cynergisuite.middleware.accounting.general.ledger.journal.entry

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationTypeDataLoader
import com.cynergisuite.middleware.store.Store
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class GeneralLedgerJournalEntryDetailDataLoader {

   static Stream<GeneralLedgerJournalEntryDetailEntity> stream(
      int numberIn = 1,
      AccountEntity accountIn,
      Store profitCenterIn,
      BigDecimal amountIn = 0
   ) {
      final number = numberIn > 0 ? numberIn : 1

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerJournalEntryDetailEntity(
            accountIn,
            BankReconciliationTypeDataLoader.random(),
            profitCenterIn,
            amountIn
         )
      }
   }

   static Stream<GeneralLedgerJournalEntryDetailDTO> streamDTO(
      int numberIn = 1,
      SimpleIdentifiableDTO accountIn,
      SimpleLegacyIdentifiableDTO profitCenterIn,
      BigDecimal amountIn = 0
   ) {
      final number = numberIn > 0 ? numberIn : 1

      return IntStream.range(0, number).mapToObj {
         new GeneralLedgerJournalEntryDetailDTO(
            accountIn,
            new BankReconciliationTypeDTO(BankReconciliationTypeDataLoader.random()),
            profitCenterIn,
            amountIn
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class GeneralLedgerJournalEntryDetailDataLoaderService {

   static GeneralLedgerJournalEntryDetailDTO singleDTO(
      SimpleIdentifiableDTO accountIn,
      SimpleLegacyIdentifiableDTO profitCenterIn,
      BigDecimal amountIn = 0
   ) {
      return GeneralLedgerJournalEntryDetailDataLoader.streamDTO(1, accountIn, profitCenterIn, amountIn)
         .findFirst().orElseThrow { new Exception("Unable to create General Ledger Journal Entry Detail DTO") }
   }
}
