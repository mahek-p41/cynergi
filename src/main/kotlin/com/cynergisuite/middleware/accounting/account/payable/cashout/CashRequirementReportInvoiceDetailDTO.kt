package com.cynergisuite.middleware.accounting.account.payable.cashout

import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "CashRequirementReportInvoiceDetailDTO", title = "Cash Requirement Report Invoice Detail", description = "Invoice detail for AP Cash Requirement report")
data class CashRequirementReportInvoiceDetailDTO(

   @field:NotNull
   @field:Schema(description = "Invoice company id")
   var invoiceCompanyId: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Invoice vendor id")
   var invoiceVendorId: UUID? = null,

   @field:NotNull
   @field:Size(max = 20)
   @field:Schema(description = "Account payable invoice", maxLength = 20)
   var invoice: String? = null,

   @field:NotNull
   @field:Schema(description = "Invoice date")
   var invoiceDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Invoice amount")
   var invoiceAmount: BigDecimal? = null,

   @field:Schema(description = "Invoice discount amount", required = false)
   var invoiceDiscountAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Invoice expense date")
   var invoiceExpenseDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Invoice paid amount")
   var invoicePaidAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Invoice discount taken")
   var invoiceDiscountTaken: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Invoice status type")
   var invoiceStatus: AccountPayableInvoiceStatusTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Invoice due date")
   var invoiceDueDate: LocalDate? = null,

   @field:Schema(description = "Account payable payment payment date", required = false)
   var apPaymentPaymentDate: LocalDate? = null,

   @field:Schema(description = "Account payable payment status id", required = false)
   var apPaymentStatusId: Int?,

   @field:Schema(description = "Account payable payment status value", required = false)
   var apPaymentStatusValue: String?,

   @field:Schema(description = "Account payable payment date voided", required = false)
   var apPaymentDateVoided: LocalDate? = null,

   @field:Schema(description = "Account payable payment detail amount", required = false)
   var apPaymentDetailAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Balance")
   var balance: BigDecimal? = null,

   @field:Schema(description = "Enum for which column the balance will display in")
   var balanceDisplay: CashRequirementBalanceEnum? = null

) {
   constructor(entity: CashRequirementReportInvoiceDetailEntity) :
      this(
         invoiceCompanyId = entity.invoiceCompanyId,
         invoiceVendorId = entity.invoiceVendorId,
         invoice = entity.invoice,
         invoiceDate = entity.invoiceDate,
         invoiceAmount = entity.invoiceAmount,
         invoiceDiscountAmount = entity.invoiceDiscountAmount,
         invoiceExpenseDate = entity.invoiceExpenseDate,
         invoicePaidAmount = entity.invoicePaidAmount,
         invoiceDiscountTaken = entity.invoiceDiscountTaken,
         invoiceStatus = AccountPayableInvoiceStatusTypeDTO(entity.invoiceStatus),
         invoiceDueDate = entity.invoiceDueDate,
         apPaymentPaymentDate = entity.apPaymentPaymentDate,
         apPaymentStatusId = entity.apPaymentStatusId,
         apPaymentStatusValue = entity.apPaymentStatusValue,
         apPaymentDateVoided = entity.apPaymentDateVoided,
         apPaymentDetailAmount = entity.apPaymentDetailAmount,
         balance = entity.balance,
         balanceDisplay = entity.balanceDisplay
      )
}
