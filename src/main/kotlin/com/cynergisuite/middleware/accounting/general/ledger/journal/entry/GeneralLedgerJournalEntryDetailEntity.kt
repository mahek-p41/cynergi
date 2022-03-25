package com.cynergisuite.middleware.accounting.general.ledger.journal.entry

import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.type.BankReconciliationType
import com.cynergisuite.middleware.store.Store
import java.math.BigDecimal

data class GeneralLedgerJournalEntryDetailEntity(
   val account: AccountEntity,
   val bankType: BankReconciliationType?,
   val profitCenter: Store,
   val amount: BigDecimal
)
