package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableCheckPreviewDTO", title = "Account Payable Check Preview DTO", description = "Account Payable Check Preview DTO")
data class AccountPayableCheckPreviewDTO(

   @field:NotNull
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int? = null,

   @field:Schema(description = "Vendor name")
   var vendorName: String? = null,

   @field:Schema(description = "Check number")
   var checkNumber: Int? = null,

   @field:Schema(description = "Distribution amount")
   var date: LocalDate? = null,

   @field:Schema(description = "Account Payable Invoices")
   var invoiceList: List<AccountPayableInvoiceDTO>? = null,
   )
