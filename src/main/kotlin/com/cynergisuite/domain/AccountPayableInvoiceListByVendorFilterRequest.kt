package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema

@Schema(
   name = "AccountPayableInvoiceListByVendorFilterRequest",
   title = "Account Payable Invoice List By Vendor Filter Request",
   description = "Filter request for Account Payable Invoice List By Vendor",
   allOf = [PageRequestBase::class]
)
@Introspected
class AccountPayableInvoiceListByVendorFilterRequest(

   page: Int? = null,
   size: Int? = null,
   sortBy: String? = null,
   sortDirection: String? = null,

   @field:Schema(name = "vendor", description = "Starting vendor number")
   var vendor: Int? = null,

   @field:Schema(name = "invoice", description = "Starting invoice number")
   var invoice: String? = null

) : PageRequestBase<AccountPayableInvoiceListByVendorFilterRequest>(page, size, sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): AccountPayableInvoiceListByVendorFilterRequest =
      AccountPayableInvoiceListByVendorFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         vendor = this.vendor,
         invoice = this.invoice
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "vendor" to vendor,
         "invoice" to invoice
      )
}
