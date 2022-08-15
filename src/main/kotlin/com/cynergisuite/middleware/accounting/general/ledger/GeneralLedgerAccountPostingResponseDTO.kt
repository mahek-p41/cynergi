package com.cynergisuite.middleware.accounting.general.ledger

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.journal.entry.GeneralLedgerJournalEntryDetailDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "General Ledger Account Posting Response DTO", title = "DTO for transferring GL Details AND GL Journal Entries", description = "DTO for transferring GL Details AND GL Journal Entries")
data class GeneralLedgerAccountPostingResponseDTO(

   @field:Schema(description = "General Ledger Summary")
   var glSummary: UUID? = null,

   @field:Schema(description = "Bank Reconciliation")
   var bankRecon: UUID? = null

)
