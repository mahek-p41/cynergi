package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerTrialBalanceReportTemplate", title = "General Ledger Trial Balance Report Template", description = "General Ledger Trial Balance Report Template")
data class GeneralLedgerTrialBalanceReportTemplate (

   @field:NotNull
   @field:Schema(description = "List of account detail DTOs", required = true)
   var accounts: List<GeneralLedgerTrialBalanceReportAccountDTO>? = null,

) {
   @field:Schema(description = "Report GL totals")
   val reportGLTotals: GeneralLedgerNetChangeDTO =
      accounts?.mapNotNull { it.glTotals }.orEmpty().reduceOrNull { accumulator, eachGLTotals ->
         accumulator + eachGLTotals
      }?: GeneralLedgerNetChangeDTO(BigDecimal.ZERO,BigDecimal.ZERO, mutableListOf(),BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)

   @field:Schema(description = "End of report totals")
   val endOfReport = GeneralLedgerTrialBalanceEndOfReportDTO(
      accounts = accounts
   )
}
