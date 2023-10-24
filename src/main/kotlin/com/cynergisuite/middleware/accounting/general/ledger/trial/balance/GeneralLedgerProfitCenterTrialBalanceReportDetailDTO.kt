package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeEntity
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailEntity
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerProfitCenterTrialBalanceReportDetail", title = "General Ledger Profit Center Trial Balance Report Detail", description = "General Ledger Profit Center Trial Balance Report Detail")
data class GeneralLedgerProfitCenterTrialBalanceReportDetailDTO(

   @field:NotNull
   @field:Schema(description = "General ledger detail date")
   var date: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "General ledger source code")
   var source: GeneralLedgerSourceCodeEntity? = null,

   @field:NotNull
   @field:Schema(description = "Journal entry number")
   var journalEntryNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "General ledger detail message")
   var message: String? = null,

   @field:NotNull
   @field:Schema(description = "General ledger detail amount")
   var amount: BigDecimal? = null

) {
   constructor(entity: GeneralLedgerDetailEntity) :
      this(
         date = entity.date,
         source = entity.source,
         journalEntryNumber = entity.journalEntryNumber,
         message = entity.message,
         amount = entity.amount
      )
}
