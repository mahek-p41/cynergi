package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerTrialBalanceAccountDetail", title = "General Ledger Trial Balance Account Detail", description = "General Ledger Trial Balance Account Detail")
data class GeneralLedgerTrialBalanceReportAccountDTO(

   @field:NotNull
   @field:Schema(description = "Account ID", required = true)
   var accountID: UUID,

   @field:NotNull
   @field:Schema(description = "Account number", required = true)
   var accountNumber: Int,

   @field:NotNull
   @field:Schema(description = "Account name", required = true)
   var accountName: String,

   @field:NotNull
   @field:Schema(description = "Account type", required = true)
   val accountType: String,

   @field:NotNull
   @field:Schema(description = "List of GL Details associated with the Account", required = true)
   var glDetails: MutableList<GeneralLedgerTrialBalanceReportDetailDTO> = mutableListOf(),

   @field:Schema(description = "GL Totals associated with the Account")
   var glTotals: GeneralLedgerNetChangeDTO? = null,
)
