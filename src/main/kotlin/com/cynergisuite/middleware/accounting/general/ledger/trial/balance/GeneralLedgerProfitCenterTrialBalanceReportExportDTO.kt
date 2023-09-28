package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "GeneralLedgerProfitCenterTrialBalanceReportExport", title = "General Ledger Profit Center Trial Balance Report Export", description = "General Ledger Profit Center Trial Balance Report Export")
data class GeneralLedgerProfitCenterTrialBalanceReportExportDTO(

   @field:NotNull
   @field:Schema(description = "Export type (D for detail line or T for total line)")
   var exportType: String? = null,

   @field:NotNull
   @field:Schema(description = "Profit center number")
   var profitCenter: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var accountNbr: Long? = null,

   @field:Schema(description = "Account name")
   var accountName: String? = null,

   @field:Schema(description = "General ledger detail date")
   var glDate: LocalDate? = null,

   @field:Schema(description = "Source code value + journal entry number + message")
   var description: String? = null,

   @field:Schema(description = "General ledger detail amount")
   var glAmount: BigDecimal? = null,

   @field:Schema(description = "Begin balance of an account/profit center pair")
   var beginBalance: BigDecimal? = null,

   @field:Schema(description = "End balance of an account/profit center pair")
   var endBalance: BigDecimal? = null,

   @field:Schema(description = "Total debits - total credits for an account/profit center pair")
   var netChange: BigDecimal? = null

)
