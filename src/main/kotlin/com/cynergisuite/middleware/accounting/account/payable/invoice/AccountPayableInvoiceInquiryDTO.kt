package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Digits
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableInvoiceInquiry", title = "Account Payable Invoice Inquiry", description = "Account payable invoice inquiry")
data class AccountPayableInvoiceInquiryDTO(


   @field:NotNull
   @field:Size(max = 20)
   @field:Schema(description = "Account payable invoice id", maxLength = 20)
   var id: UUID? = null,

   @field:NotNull
   @field:Size(max = 20)
   @field:Schema(description = "Account payable invoice", maxLength = 20)
   var invoice: String? = null,

   @field:NotNull
   @field:Schema(description = "Invoice amount")
   var invAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Invoice date")
   var invDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice type")
   var type: AccountPayableInvoiceTypeDTO? = null,

   @field:Schema(description = "Separate check indicator")
   var separateCheckIndicator: Boolean? = null,

   @field:Schema(description = "Purchase order ID", required = false)
   val poId: UUID? = null,

   @field:Schema(description = "Purchase order number", required = false)
   var poNbr: Int? = null,

   @field:NotNull
   @field:Schema(description = "Use tax indicator")
   var useTaxIndicator: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Due date")
   var dueDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Expense date")
   var expenseDate: LocalDate? = null,

   @field:Schema(description = "Discount date", required = false)
   var discountDate: LocalDate? = null,

   @field:Schema(description = "Discount basis amount", required = false)
   var discountBasisAmount: BigDecimal? = null,

   @field:Schema(description = "Discount taken", required = false)
   var discountTaken: BigDecimal? = null,

   @field:DecimalMin(value = "0", inclusive = false)
   @field:DecimalMax(value = "1", inclusive = false)
   @field:Digits(integer = 1, fraction = 7)
   @field:Schema(description = "Discount percent", required = false)
   var discountPercent: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice status type")
   var status: AccountPayableInvoiceStatusTypeDTO? = null,

   @field:Schema(description = "List of account payable payments")
   var payments: List<AccountPayableInvoiceInquiryPaymentDTO>? = null,

   @field:Schema(description = "List of account payable invoice distributions")
   var glDist: List<AccountPayableDistDetailReportDTO>? = null,

   @field:Schema(description = "Message", required = false)
   var message: String? = null

)
