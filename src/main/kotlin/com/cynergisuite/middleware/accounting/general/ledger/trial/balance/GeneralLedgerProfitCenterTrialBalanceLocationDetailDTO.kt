package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerProfitCenterTrialBalanceLocationDetail", title = "General Ledger Profit Center Trial Balance Location Detail", description = "General Ledger Profit Center Trial Balance Location Detail")
data class GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO(

   @field:NotNull
   @field:Schema(description = "Profit center number")
   var profitCenter: Int? = null,

   @field:NotNull
   @field:Schema(description = "List of account detail DTOs")
   var accountDetailList: List<GeneralLedgerProfitCenterTrialBalanceAccountDetailDTO>? = null,

   @field:NotNull
   @field:Schema(description = "Location totals")
   var locationTotals: GeneralLedgerNetChangeDTO? = null

)
