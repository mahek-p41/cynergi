package com.cynergisuite.middleware.accounting.general.ledger.journal.entry

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import java.math.BigDecimal
import java.time.LocalDate

data class GeneralLedgerJournalEntryEntity(
  val entryDate: LocalDate,
  val source: GeneralLedgerSourceCodeEntity,
  val reverse: Boolean,
  val useTemplate: Boolean,
  val journalEntryNumber: Int,
  val journalEntryDetails: MutableList<GeneralLedgerJournalEntryDetailEntity>,
  val balance: BigDecimal,
  val message: String? = null,
  val postReversingEntry: Boolean
)
