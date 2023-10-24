package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "CashFlowFilterRequest",
   title = "Cash Flow Report Filter Request",
   description = "Filter request for Cash Flow Report",
   allOf = [SortableRequestBase::class]
)
class CashFlowFilterRequest(

   @field:Schema(description = "details")
   var details: Boolean? = false,

   @field:Schema(name = "entryDate", description = "From date one for cash flow report")
   var fromDateOne: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date one for cash flow report")
   var thruDateOne: LocalDate? = null,

   @field:Schema(name = "entryDate", description = "From date two for cash flow report")
   var fromDateTwo: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date two for cash flow report")
   var thruDateTwo: LocalDate? = null,

   @field:Schema(name = "entryDate", description = "From date three for cash flow report")
   var fromDateThree: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date three for cash flow report")
   var thruDateThree: LocalDate? = null,

   @field:Schema(name = "entryDate", description = "From date four for cash flow report")
   var fromDateFour: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date four for cash flow report")
   var thruDateFour: LocalDate? = null,

   @field:Schema(name = "entryDate", description = "From date five for cash flow report")
   var fromDateFive: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date five for cash flow report")
   var thruDateFive: LocalDate? = null,

) : SortableRequestBase<CashFlowFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "details" to details,
         "fromDateOne" to fromDateOne,
         "thruDateOne" to thruDateOne,
         "fromDateTwo" to fromDateTwo,
         "thruDateTwo" to thruDateTwo,
         "fromDateThree" to fromDateThree,
         "thruDateThree" to thruDateThree,
         "fromDateFour" to fromDateFour,
         "thruDateFour" to thruDateFour,
         "fromDateFive" to fromDateFive,
         "thruDateFive" to thruDateFive
      )
}
