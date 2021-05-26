package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.cynergisuite.middleware.vendor.VendorDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.*
import javax.validation.constraints.NotNull

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayablePaymentDetail", title = "Account Payable Payment Detail", description = "Account Payable Payment Detail")
data class AccountPayablePaymentDetailDTO(

   @field:Schema(description = "Account Payable Payment Detail id")
   var id: UUID? = null,

   @field:Schema(description = "Vendor dto")
   var vendor: VendorDTO? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice dto")
   var invoice: AccountPayableInvoiceDTO? = null,

   @field:NotNull
   @field:Schema(description = "Account payable payment id dto")
   var payment: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(description = "Invoice amount")
   var invoiceAmount: BigDecimal? = null,

   @field:Schema(description = "Discount amount", required = false)
   var discountAmount: BigDecimal? = null

) : Identifiable {
   constructor(entity: AccountPayablePaymentDetailEntity) :
      this(
         id = entity.id,
         vendor = entity.vendor?.let { VendorDTO(it) },
         invoice = AccountPayableInvoiceDTO(entity.invoice),
         payment = entity.payment?.let { SimpleIdentifiableDTO(it) },
         invoiceAmount = entity.amount,
         discountAmount = entity.discount
      )

   override fun myId(): UUID? = id
}
