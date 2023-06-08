package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

@Schema(
   name = "AccountPayableCheckPreviewFilterRequest",
   title = "Resulting list for filtering result",
   description = "Defines the parameters available for a sortable request.",
   allOf = [SortableRequestBase::class]
)
class AccountPayableCheckPreviewFilterRequest(

   @field:Schema(name = "bank", description = "Bank number")
   var bank: Long,

   @field:Schema(name = "checkNumber", description = "Check Number")
   var checkNumber: Int,

   @field:Schema(name = "checkDate", description = "Check date")
   var checkDate: LocalDate? = null,

   @field:Schema(name = "dueDate", description = "Due date")
   var dueDate: LocalDate? = null,

   @field:Schema(name = "discountDate", description = "Discount date")
   var discountDate: LocalDate? = null,

   @field:Schema(name = "vendorGroup", description = "Vendor group")
   var vendorGroup: UUID? = null,

   @field:Schema(name = "printNotes", description = "Print notes")
   var printNotes: Boolean? = null

) : SortableRequestBase<AccountPayableCheckPreviewFilterRequest>(null, null) {

   override fun sortByMe(): String = sortBy()

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "bank" to bank,
         "checkNumber" to checkNumber,
         "checkDate" to checkDate,
         "dueDate" to dueDate,
         "discountDate" to discountDate,
         "vendorGroup" to vendorGroup,
         "printNotes" to printNotes
      )
}
