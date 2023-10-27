package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.middleware.accounting.account.AccountEntity
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "TrialBalanceWorksheetDetailDTO", title = "Trial Balance Worksheet Detail", description = "Trial Balance Worksheet Detail")
data class TrialBalanceWorksheetDetailDTO(

   @field:NotNull
   @field:Schema(description = "Account number", required = true)
   var account: Long,

   @field:Schema(description = "Description")
   var description: String? = null,

   @field:NotNull
   @field:Schema(description = "Debits")
   var debits: BigDecimal? = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Credits")
   var credits: BigDecimal? = BigDecimal.ZERO
   )
{
   constructor(account: AccountEntity, debit: BigDecimal?, credit: BigDecimal?) :
      this(
         account = account.number,
         description = account.name,
         debits = debit,
         credits = credit
      )
}
