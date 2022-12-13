package com.cynergisuite.middleware.accounting.account.payable.cashflow

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Schema(name = "CashRequirementBalanceDTO", title = "Balance Display Totals", description = "Totals for each week entered in filter")
data class CashFlowBalanceDTO(

   @field:NotNull
   @field:Schema(description = "Date one amount")
   var dateOneAmount: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date two amount")
   var dateTwoAmount: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date three amount")
   var dateThreeAmount: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date four amount")
   var dateFourAmount: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Date five amount")
   var dateFiveAmount: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "Discount Taken")
   var discountTaken: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "Discount Lost")
   var discountLost: BigDecimal = BigDecimal.ZERO,

   @field:Schema(description = "Discount Date")
   var discountDate: LocalDate?
) {
   constructor(entity: CashFlowBalanceEntity) :
      this(
         dateOneAmount = entity.dateOneAmount,
         dateTwoAmount = entity.dateTwoAmount,
         dateThreeAmount = entity.dateThreeAmount,
         dateFourAmount = entity.dateFourAmount,
         dateFiveAmount = entity.dateFiveAmount,
         discountTaken = entity.discountTaken,
         discountLost = entity.discountLost,
         discountDate = entity.discountDate
      )
}
