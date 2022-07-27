package com.cynergisuite.middleware.accounting.general.ledger.journal.entry

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import java.time.LocalDate

data class GeneralLedgerJournalEntryEntity(
  val entryDate: LocalDate,
  val source: GeneralLedgerSourceCodeEntity,
  val reverse: Boolean,
  val journalEntryNumber: Int,
  val journalEntryDetails: MutableList<GeneralLedgerJournalEntryDetailEntity>,
  val message: String? = null,
  val postReversingEntry: Boolean
)
