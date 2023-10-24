package com.cynergisuite.middleware.accounting.account.payable.aging

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Schema(name = "BalanceDisplayTotals", title = "Balance Display Totals", description = "Totals for Balance, Current, 1 to 30, 31 to 60, and Over 60 columns")
data class BalanceDisplayTotalsDTO(

   @field:NotNull
   @field:Schema(description = "Balance column total")
   var balanceTotal: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Current column total")
   var currentTotal: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "1 to 30 column total")
   var oneToThirtyTotal: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "31 to 60 column total")
   var thirtyOneToSixtyTotal: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Over 60 column total")
   var overSixtyTotal: BigDecimal = BigDecimal.ZERO

) {
   constructor(entity: BalanceDisplayTotalsEntity) :
      this(
         balanceTotal = entity.balanceTotal,
         currentTotal = entity.currentTotal,
         oneToThirtyTotal = entity.oneToThirtyTotal,
         thirtyOneToSixtyTotal = entity.thirtyOneToSixtyTotal,
         overSixtyTotal = entity.overSixtyTotal
      )
}
