package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "AccountPayableRecurringInvoiceTransferFilterRequest",
   title = "Account Payable Recurring Invoice Transfer Filter Request",
   description = "Filter request for Account Payable Recurring Invoice Transfer",
   allOf = [PageRequestBase::class]
)
@Introspected
class AccountPayableRecurringInvoiceTransferFilterRequest(

   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "beginVendor", description = "Starting vendor number")
   var beginVendor: Int? = null,

   @field:Schema(name = "endVendor", description = "Ending vendor number")
   var endVendor: Int? = null,

   @field:Schema(name = "sourceCode", description = "Source code")
   var sourceCode: String? = null,

   @field:Schema(name = "entryDate" , description = "Entry date")
   var entryDate: LocalDate? = null,

   @field:Schema(name = "preview", description = "Preview flag")
   var preview: Boolean? = false

) : PageRequestBase<AccountPayableRecurringInvoiceTransferFilterRequest>(page, size, sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): AccountPayableRecurringInvoiceTransferFilterRequest =
      AccountPayableRecurringInvoiceTransferFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         beginVendor = this.beginVendor,
         endVendor = this.endVendor,
         sourceCode = this.sourceCode,
         entryDate = this.entryDate,
         preview = this.preview
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginVendor" to beginVendor,
         "endVendor" to endVendor,
         "sourceCode" to sourceCode,
         "entryDate" to entryDate,
         "preview" to preview
      )
}
