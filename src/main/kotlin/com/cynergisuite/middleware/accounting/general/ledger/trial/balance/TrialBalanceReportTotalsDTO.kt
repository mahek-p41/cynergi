package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Schema(name = "TrialBalanceReportTotals", title = "Trial Balance Report Totals", description = "Totals for Account Totals, Location Totals, and Report Totals")
data class TrialBalanceReportTotalsDTO(

   @field:NotNull
   @field:Schema(description = "Beginning balance")
   var begBalance: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Debit amount")
   var debit: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Credit amount")
   var credit: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Net change")
   var netChange: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Closing balance")
   var endBalance: BigDecimal = BigDecimal.ZERO

)
