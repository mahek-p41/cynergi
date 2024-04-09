package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(
   name = "AccountPayableRecurringInvoiceReportFilterRequest",
   title = "Account Payable Recurring Invoice Report Filter Request",
   description = "Filter request for Account Payable Recurring Invoice Report",
   allOf = [PageRequestBase::class]
)
@Introspected
class AccountPayableRecurringInvoiceReportFilterRequest(

   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "beginVendor", description = "Starting vendor number")
   var beginVendor: Int? = null,

   @field:Schema(name = "endVendor", description = "Ending vendor number")
   var endVendor: Int? = null

) : PageRequestBase<AccountPayableRecurringInvoiceReportFilterRequest>(page, size, sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): AccountPayableRecurringInvoiceReportFilterRequest =
      AccountPayableRecurringInvoiceReportFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         beginVendor = this.beginVendor,
         endVendor = this.endVendor
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "beginVendor" to beginVendor,
         "endVendor" to endVendor
      )
}
