package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerProfitCenterTrialBalanceReportTemplate", title = "General Ledger Profit Center Trial Balance Report Template", description = "General Ledger Profit Center Trial Balance Report Template")
data class TrialBalanceWorksheetReportTemplate (

   @field:NotNull
   @field:Schema(description = "List of location detail DTOs")
   var trialBalanceList: List<TrialBalanceWorksheetDetailDTO>? = null,

   @field:NotNull
   @field:Schema(description = "Credit totals")
   var creditTotals: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Debit totals")
   var debitTotals: BigDecimal? = null,

)
