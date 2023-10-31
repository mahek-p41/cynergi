package com.cynergisuite.middleware.inventory

import com.fasterxml.jackson.annotation.JsonView
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import org.jetbrains.annotations.NotNull

@Introspected
@JsonView
@Schema(name = "Inventory End Of Month", title = "Inventory End of Month Item", description = "Single item in inventory end of month")
data class InventoryEndOfMonthDTO(

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
   var reportTotal: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var glBalance: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account number")
   var difference: Int? = null,
)  {
   constructor(entity: InventoryEndOfMonthEntity) :
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
