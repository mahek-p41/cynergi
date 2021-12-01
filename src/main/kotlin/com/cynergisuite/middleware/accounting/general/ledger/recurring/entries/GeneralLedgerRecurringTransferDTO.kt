package com.cynergisuite.middleware.accounting.general.ledger.recurring.entries

import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.*
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "GeneralLedgerRecurringTransfer", title = "General Ledger Recurring Transfer", description = "General ledger recurring transfer entity")
data class GeneralLedgerRecurringTransferDTO(

   @field:NotNull
   @field:Schema(description = "General ledger recurring id")
   var generalLedgerRecurringId: UUID? = null,

   @field:Schema(description = "General ledger recurring last transfer date", required = false)
   var lastTransferDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "General ledger detail")
   var generalLedgerDetail: GeneralLedgerDetailDTO? = null

) {}
