package com.cynergisuite.middleware.accounting.general.ledger.reconciliation

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import org.jetbrains.annotations.NotNull

@JsonInclude(NON_NULL)
data class GeneralLedgerReconciliationInventoryDTO(

   @field:NotNull
   @field:Schema(description = "Account number")
   var accountNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account name")
   var accountName: String? = null,

   @field:NotNull
   @field:Schema(description = "Store number")
   var storeNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var deprUnits: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var nonDepr: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var reportTotal: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var glBalance: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var difference: BigDecimal? = null,
)  {
   constructor(entity: GeneralLedgerReconciliationInventoryEntity) :
      this(
         accountNumber = entity.accountNumber,
         accountName = entity.accountName,
         storeNumber = entity.storeNumber,
         deprUnits = entity.deprUnits,
         nonDepr = entity.nonDepr,
         reportTotal = entity.reportTotal,
         glBalance = entity.glBalance,
         difference = entity.difference
      )
}
