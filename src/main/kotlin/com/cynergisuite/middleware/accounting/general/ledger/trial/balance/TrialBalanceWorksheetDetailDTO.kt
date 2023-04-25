package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "TrialBalanceWorksheetDetailDTO", title = "Trial Balance Worksheet Detail", description = "Trial Balance Worksheet Detail")
data class TrialBalanceWorksheetDetailDTO(

   @field:NotNull
   @field:Schema(description = "Account number")
   var account: Int? = null,

   @field:NotNull
   @field:Schema(description = "Description")
   var description: String? = null,

   @field:NotNull
   @field:Schema(description = "Debits")
   var debits: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Credits")
   var credits: BigDecimal? = null
   )
