package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "TrialBalanceWorksheetReportTemplate", title = "Trial Balance Worksheet Report Template", description = "Trial Balance Worksheet Report Template")
data class TrialBalanceWorksheetReportTemplate (

   @field:NotNull
   @field:Schema(description = "List of location detail DTOs", required = true)
   var accounts: List<TrialBalanceWorksheetDetailDTO>,

   @field:NotNull
   @field:Schema(description = "Credit totals", required = true)
   var creditTotals: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Debit totals", required = true)
   var debitTotals: BigDecimal? = BigDecimal.ZERO,
)
