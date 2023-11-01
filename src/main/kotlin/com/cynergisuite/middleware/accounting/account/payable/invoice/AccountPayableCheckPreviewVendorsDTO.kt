package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableCheckPreviewDTO", title = "Account Payable Check Preview DTO", description = "Account Payable Check Preview DTO")
data class AccountPayableCheckPreviewVendorsDTO(


   @field:NotNull
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int? = null,

   @field:Schema(description = "Vendor name")
   var vendorName: String? = null,

   @field:Schema(description = "Address1")
   var address1: String? = null,

   @field:Schema(description = "Address1")
   var address2: String? = null,

   @field:Schema(description = "City")
   var city: String? = null,

   @field:Schema(description = "State")
   var state: String? = null,

   @field:Schema(description = "Postal Code")
   var postalCode: String? = null,

   @field:Schema(description = "Check number")
   var checkNumber: Int? = null,

   @field:Schema(description = "Distribution amount")
   var date: LocalDate? = null,

   @field:Schema(description = "Account Payable Invoices")
   var invoiceList: MutableList<AccountPayableCheckPreviewInvoiceDTO> = mutableListOf(),

   @field:Schema(description = "Invoice Gross Total")
   var gross: BigDecimal,

   @field:Schema(description = "Invoice Discount Total")
   var discount: BigDecimal,

   @field:Schema(description = "Invoice Deduction Total")
   var deduction: BigDecimal,

   @field:Schema(description = "Invoice Net Paid Total")
   var netPaid: BigDecimal
) {
   constructor(entity: AccountPayableCheckPreviewVendorsEntity) :
      this(
         vendorNumber = entity.vendorNumber,
         vendorName = entity.vendorName,
         address1 = entity.address1,
         address2 = entity.address2,
         city = entity.city,
         state = entity.state,
         postalCode = entity.postalCode,
         checkNumber = entity.checkNumber,
         date = entity.date,
         invoiceList = entity.invoiceList!!.asSequence().map { apInvoiceEntity ->
            AccountPayableCheckPreviewInvoiceDTO(apInvoiceEntity)}.toMutableList(),
         gross = entity.gross,
         discount = entity.discount,
         deduction = entity.deduction,
         netPaid = entity.netPaid
      )
}
