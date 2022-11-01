package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.middleware.accounting.account.AccountDTO
import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerProfitCenterTrialBalanceAccountDetail", title = "General Ledger Profit Center Trial Balance Account Detail", description = "General Ledger Profit Center Trial Balance Account Detail")
data class GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO(

   @field:NotNull
   @field:Schema(description = "Account DTO")
   var account: AccountDTO? = null,

   @field:NotNull
   @field:Schema(description = "List of report detail DTOs")
   var reportDetailList: List<GeneralLedgerProfitCenterTrialBalanceReportDetailDTO>? = null,

   @field:NotNull
   @field:Schema(description = "Account totals")
   var accountTotals: GeneralLedgerNetChangeDTO? = null

)
