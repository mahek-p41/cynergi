package com.cynergisuite.middleware.accounting.account.payable.cashout

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import javax.validation.constraints.NotNull

@Schema(name = "CashRequirementBalanceDTO", title = "Balance Display Totals", description = "Totals for each week entered in filter")
data class CashRequirementBalanceDTO(


   @field:NotNull
   @field:Schema(description = "Week one total")
   var weekOnePaid: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Week one total")
   var weekOneDue: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Week two total")
   var weekTwoPaid: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Week one total")
   var weekTwoDue: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Week three total")
   var weekThreePaid: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Week one total")
   var weekThreeDue: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Week four total")
   var weekFourPaid: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Week one total")
   var weekFourDue: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Week five total")
   var weekFivePaid: BigDecimal = BigDecimal.ZERO,

   @field:NotNull
   @field:Schema(description = "Week one total")
   var weekFiveDue: BigDecimal = BigDecimal.ZERO,
) {
   constructor(entity: CashRequirementBalanceEntity) :
      this(
         weekOnePaid = entity.weekOnePaid,
         weekOneDue = entity.weekOneDue,
         weekTwoPaid = entity.weekTwoPaid,
         weekTwoDue = entity.weekTwoDue,
         weekThreePaid = entity.weekThreePaid,
         weekThreeDue = entity.weekThreeDue,
         weekFourPaid = entity.weekFourPaid,
         weekFourDue = entity.weekFourDue,
         weekFivePaid = entity.weekFivePaid,
         weekFiveDue = entity.weekFiveDue
      )
}
