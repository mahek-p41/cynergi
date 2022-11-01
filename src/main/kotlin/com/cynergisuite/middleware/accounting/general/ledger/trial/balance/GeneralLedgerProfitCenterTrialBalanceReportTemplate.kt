package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.middleware.accounting.general.ledger.inquiry.GeneralLedgerNetChangeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerProfitCenterTrialBalanceReportTemplate", title = "General Ledger Profit Center Trial Balance Report Template", description = "General Ledger Profit Center Trial Balance Report Template")
data class GeneralLedgerProfitCenterTrialBalanceReportTemplate (

   @field:NotNull
   @field:Schema(description = "List of location detail DTOs")
   var locationDetailList: List<GeneralLedgerProfitCenterTrialBalanceLocationDetailDTO>? = null,

   @field:NotNull
   @field:Schema(description = "Report totals")
   var reportTotals: GeneralLedgerNetChangeDTO? = null

)
