package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerTrialBalanceReportDetail", title = "General Ledger Trial Balance Report Detail", description = "General Ledger Trial Balance Report Detail")
data class GeneralLedgerTrialBalanceReportDetailDTO(

   @field:NotNull
   @field:Schema(description = "General Ledger detail ID", required = true)
   var id: UUID,

   @field:NotNull
   @field:Schema(description = "General ledger detail date", required = true)
   var date: LocalDate,

   @field:NotNull
   @field:Schema(description = "General ledger source code", required = true)
   var source: GeneralLedgerSourceCodeDTO,

   @field:Schema(description = "Journal entry number")
   var journalEntryNumber: Int?,

   @field:Schema(description = "General ledger detail message")
   var message: String?,

   @field:NotNull
   @field:Schema(description = "General ledger detail amount", required = true)
   var amount: BigDecimal

)
