package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "CashRequirementFilterRequest",
   title = "Cash Requirements Report Filter Request",
   description = "Filter request for Cash Requirements Report",
   allOf = [SortableRequestBase::class]
)
class CashRequirementFilterRequest(

   @field:Schema(description = "Beginning Vendor")
   var beginVendor: Int? = null,

   @field:Schema(description = "End Vendor")
   var endVendor: Int? = null,

   @field:Schema(description = "details")
   var details: Boolean? = false,

   @field:Schema(name = "entryDate", description = "From date for general ledger journal")
   var fromDateOne: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger journal")
   var thruDateOne: LocalDate? = null,

   @field:Schema(name = "entryDate", description = "From date for general ledger journal")
   var fromDateTwo: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger journal")
   var thruDateTwo: LocalDate? = null,

   @field:Schema(name = "entryDate", description = "From date for general ledger journal")
   var fromDateThree: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger journal")
   var thruDateThree: LocalDate? = null,

   @field:Schema(name = "entryDate", description = "From date for general ledger journal")
   var fromDateFour: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger journal")
   var thruDateFour: LocalDate? = null,

   @field:Schema(name = "entryDate", description = "From date for general ledger journal")
   var fromDateFive: LocalDate? = null,

   @field:Schema(name = "thruDate", description = "Thru date for general ledger journal")
   var thruDateFive: LocalDate? = null,

) : SortableRequestBase<CashRequirementFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginVendor" to beginVendor,
         "endVendor" to endVendor,
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
