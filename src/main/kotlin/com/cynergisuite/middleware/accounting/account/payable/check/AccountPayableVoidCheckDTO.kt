package com.cynergisuite.middleware.accounting.account.payable.check

import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull

@JsonView
@Schema(name = "AccountPayableVoidCheck", title = "Account Payable Void Check", description = "Account Payable Void Check")
data class AccountPayableVoidCheckDTO(

   @field:NotNull
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int? = null,

   @field:NotNull
   @field:Schema(description = "Vendor name")
   var vendorName: String? = null,

   @field:NotNull
   @field:Schema(description = "Bank ID")
   var bankId: UUID,

   @field:NotNull
   @field:Schema(description = "Check Number")
   var checkNumber: String,

   @field:NotNull
   @field:Schema(description = "Amount")
   var amount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Payment Status")
   var paymentStatus: Int? = null,

   @field:NotNull
   @field:Schema(description = "Date Cleared")
   var dateCleared: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Date")
   var date: LocalDate? = null,

   @field:Schema(description = "Effective Date")
   var effectiveDate: LocalDate? = null,

   @field:Schema(description = "List of Invoices")
   var invoices: MutableList<AccountPayableInvoiceDTO>? = mutableListOf(),
   )
