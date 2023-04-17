package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerTrialBalanceReportTemplate", title = "General Ledger Trial Balance Report Template", description = "General Ledger Trial Balance Report Template")
data class GeneralLedgerTrialBalanceReportTemplate (

   @field:NotNull
   @field:Schema(description = "List of account detail DTOs", required = true)
   var accounts: List<GeneralLedgerTrialBalanceReportAccountDTO>? = null,

   @field:NotNull
   @field:Schema(description = "Report totals")
   var reportTotals: GeneralLedgerNetChangeDTO? = null,

   // Todo endOfReportTotals will be replace by some getter functions to save computations in repository

)
