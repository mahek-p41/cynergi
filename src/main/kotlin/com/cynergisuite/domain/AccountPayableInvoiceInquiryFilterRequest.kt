package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@Schema(
   name = "AccountPayableInvoiceInquiryFilterRequest",
   title = "Account Payable Invoice Inquiry Filter Request",
   description = "Filter request for Account Payable Invoice inquiry",
   allOf = [PageRequestBase::class]
)
@Introspected
class AccountPayableInvoiceInquiryFilterRequest(
   page: Int? = null,
   size: Int? = null,
   @field:Pattern(regexp = "poHeader.number|apInvoice.invoice|apInvoice.invoice_date|apInvoice.due_date|apInvoice.invoice_amount")
   @field:Schema(description = "The column to sort the AP invoice inquiry by (poHeader.number|apInvoice.invoice|apInvoice.invoice_date|apInvoice.due_date|apInvoice.invoice_amount).", defaultValue = "poHeader.number")
   override var sortBy: String? = null,
   sortDirection: String? = null,

   @field:NotNull
   @field:Schema(name = "vendor", description = "Vendor number")
   var vendor: Int? = null,

   @field:NotNull
   @field:Schema(name = "payTo", description = "Pay to vendor number")
   var payTo: Int? = null,

   @field:Schema(name = "invStatus", description = "Invoice status type value")
   var invStatus: String? = null,

   @field:Schema(name = "poNbr", description = "Purchase order number")
   var poNbr: Int? = null,

   @field:Schema(name = "invNbr", description = "Invoice number")
   var invNbr: String? = null,

   @field:Schema(name = "invDate", description = "Invoice date")
   var invDate: LocalDate? = null,

   @field:Schema(name = "dueDate", description = "Due date")
   var dueDate: LocalDate? = null,

   @field:Schema(name = "invAmount", description = "Invoice amount")
   var invAmount: BigDecimal? = null

) : PageRequestBase<AccountPayableInvoiceInquiryFilterRequest>(page, size, sortBy, sortDirection) {

   override fun sortByMe(): String = sortBy()

   override fun myCopyPage(page: Int, size: Int, sortBy: String, sortDirection: String): AccountPayableInvoiceInquiryFilterRequest =
      AccountPayableInvoiceInquiryFilterRequest(
         page = page,
         size = size,
         sortBy = sortBy,
         sortDirection = sortDirection,
         vendor = this.vendor,
         payTo = this.payTo,
         invStatus = this.invStatus,
         poNbr = this.poNbr,
         invNbr = this.invNbr,
         invDate = this.invDate,
         dueDate = this.dueDate,
         invAmount = this.invAmount
      )

   override fun myToStringValues(): List<Pair<String, Any?>> =
      listOf(
         "vendor" to vendor,
         "payTo" to payTo,
         "invStatus" to invStatus,
         "poNbr" to poNbr,
         "invNbr" to invNbr,
         "invDate" to invDate,
         "dueDate" to dueDate,
         "invAmount" to invAmount
      )
}
