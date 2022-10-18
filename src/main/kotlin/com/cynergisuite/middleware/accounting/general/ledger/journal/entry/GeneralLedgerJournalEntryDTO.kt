package com.cynergisuite.middleware.accounting.general.ledger.journal.entry

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerJournalEntry", title = "General Ledger Journal Entry", description = "General ledger journal entry")
data class GeneralLedgerJournalEntryDTO(

   @field:NotNull
   @field:Schema(name = "entryDate", description = "Entry date (GL period must be open)", required = true)
   var entryDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(name = "source", description = "General ledger source code", required = true)
   var source: GeneralLedgerSourceCodeDTO? = null,

   @field:NotNull
   @field:Schema(name = "reverse", description = "Determines if GL Reversal records will be created", required = true)
   var reverse: Boolean? = null,

   @field:Schema(name = "journalEntryNumber", description = "Journal entry number (system generated)")
   var journalEntryNumber: Int? = null,

   @field:Schema(name = "journalEntryDetails", description = "List of journal entry details")
   var journalEntryDetails: MutableList<GeneralLedgerJournalEntryDetailDTO> = mutableListOf(),

   @field:Schema(name = "message", description = "Message")
   var message: String? = null,

   @field:Schema(name = "postReversingEntry", description = "Determines if the GL Reversal Entry will be posted (only available if the GL is open for the reversal date)")
   var postReversingEntry: Boolean? = false,

   @field:Schema(name = "reversalId", description = "GL Reversal id, if a GL Reversal Entry was created but not posted", required = false)
   var reversalId: UUID? = null

)
