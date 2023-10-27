package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableInvoiceListByVendor", title = "Account Payable Invoice List By Vendor", description = "Account payable invoice list by vendor")
data class AccountPayableInvoiceListByVendorDTO(

   @field:Positive
   @field:Schema(description = "Vendor number")
   var vendorNumber: Int? = null,

   @field:NotNull
   @field:NotBlank
   @field:Size(min = 1, max = 30)
   @field:Schema(description = "Vendor name")
   var vendorName: String? = null,

   @field:NotNull
   @field:Size(max = 20)
   @field:Schema(description = "Account payable invoice", maxLength = 20)
   var invoice: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice date")
   var invoiceDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice amount")
   var invoiceAmount: BigDecimal? = null,

   @field:Schema(description = "Purchase order number", required = false)
   var poNbr: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice status type")
   var status: AccountPayableInvoiceStatusTypeDTO? = null

)
