package com.cynergisuite.middleware.accounting.account.payable.payment

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.invoice.AccountPayableInvoiceDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AccountPayablePaymentDetail", title = "Account Payable Payment Detail", description = "Account Payable Payment Detail")
data class AccountPayablePaymentDetailDTO(

   @field:Schema(description = "Account Payable Payment Detail id")
   var id: UUID? = null,

   @field:Schema(description = "Vendor dto")
   var vendor: SimpleIdentifiableDTO? = null,

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
         vendor = entity.vendor?.let { SimpleIdentifiableDTO(it) },
         invoice = AccountPayableInvoiceDTO(entity.invoice),
         payment = entity.payment?.let { SimpleIdentifiableDTO(it) },
         invoiceAmount = entity.amount,
         discountAmount = entity.discount
      )

   override fun myId(): UUID? = id
}
