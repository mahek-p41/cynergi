package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentStatusTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableInvoiceInquiryPayment", title = "Account Payable Invoice Inquiry Payment", description = "Account Payable Invoice Inquiry Payment")
data class AccountPayableInvoiceInquiryPaymentDTO(

   @field:NotNull
   @field:Schema(description = "Bank number")
   var bankNbr: Int? = null,

   @field:NotNull
   @field:Schema(description = "Account payable payment number", maxLength = 20)
   var paymentNbr: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable payment detail amount")
   var paid: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account payable payment date")
   var date: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable payment amount")
   var paymentAmt: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice original invoice amount")
   var originalAmt: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice status")
   var status: String? = null

)
