package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusTypeDTO
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceTypeDTO
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import javax.validation.constraints.Digits
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "AccountPayableInvoice", title = "Account Payable Invoice", description = "Account payable invoice")
data class AccountPayableInvoiceDTO(

   @field:Schema(description = "Account payable invoice id")
   var id: UUID? = null,

   @field:NotNull
   @field:Schema(description = "Vendor id")
   var vendor: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Size(max = 20)
   @field:Schema(description = "Account payable invoice", maxLength = 20)
   var invoice: String? = null,

   @field:Schema(description = "Purchase order id", required = false)
   var purchaseOrder: SimpleIdentifiableDTO? = null,

   @field:NotNull
   @field:Schema(description = "Invoice date")
   var invoiceDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Invoice amount")
   var invoiceAmount: BigDecimal? = null,

   @field:Schema(description = "Discount amount", required = false)
   var discountAmount: BigDecimal? = null,

   @field:Digits(integer = 1, fraction = 6)
   @field:Schema(description = "Discount percent", required = false)
   var discountPercent: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Auto distribution applied")
   var autoDistributionApplied: Boolean? = null,

   @field:Schema(description = "Discount taken", required = false)
   var discountTaken: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Entry date")
   var entryDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Expense date")
   var expenseDate: LocalDate? = null,

   @field:Schema(description = "Discount date", required = false)
   var discountDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Employee number")
   var employee: EmployeeValueObject? = null,

   @field:NotNull
   @field:Schema(description = "Original invoice amount")
   var originalInvoiceAmount: BigDecimal? = null,

   @field:Schema(description = "Message", required = false)
   var message: String? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice selected type")
   var selected: AccountPayableInvoiceSelectedTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Multiple payment indicator")
   var multiplePaymentIndicator: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Paid amount")
   var paidAmount: BigDecimal? = null,

   @field:Schema(description = "Selected amount", required = false)
   var selectedAmount: BigDecimal? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice type")
   var type: AccountPayableInvoiceTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Account payable invoice status type")
   var status: AccountPayableInvoiceStatusTypeDTO? = null,

   @field:NotNull
   @field:Schema(description = "Due date")
   var dueDate: LocalDate? = null,

   @field:NotNull
   @field:Schema(description = "Pay to vendor id")
   var payTo: SimpleIdentifiableDTO? = null,

   @field:Schema(description = "Separate check indicator")
   var separateCheckIndicator: Boolean? = null,

   @field:NotNull
   @field:Schema(description = "Use tax indicator")
   var useTaxIndicator: Boolean? = null,

   @field:Schema(description = "Receive date", required = false)
   var receiveDate: LocalDate? = null,

   @field:Schema(description = "Location", required = false)
   var location: SimpleLegacyIdentifiableDTO? = null

) : Identifiable {
   constructor(entity: AccountPayableInvoiceEntity) :
      this(
         id = entity.id,
         vendor = SimpleIdentifiableDTO(entity.vendor),
         invoice = entity.invoice,
         purchaseOrder = SimpleIdentifiableDTO(entity.purchaseOrder?.myId()),
         invoiceDate = entity.invoiceDate,
         invoiceAmount = entity.invoiceAmount,
         discountAmount = entity.discountAmount,
         discountPercent = entity.discountPercent,
         autoDistributionApplied = entity.autoDistributionApplied,
         discountTaken = entity.discountTaken,
         entryDate = entity.entryDate,
         expenseDate = entity.expenseDate,
         discountDate = entity.discountDate,
         employee = EmployeeValueObject(entity.employee),
         originalInvoiceAmount = entity.originalInvoiceAmount,
         message = entity.message,
         selected = AccountPayableInvoiceSelectedTypeDTO(entity.selected),
         multiplePaymentIndicator = entity.multiplePaymentIndicator,
         paidAmount = entity.paidAmount,
         selectedAmount = entity.selectedAmount,
         type = AccountPayableInvoiceTypeDTO(entity.type),
         status = AccountPayableInvoiceStatusTypeDTO(entity.status),
         dueDate = entity.dueDate,
         payTo = SimpleIdentifiableDTO(entity.payTo),
         separateCheckIndicator = entity.separateCheckIndicator,
         useTaxIndicator = entity.useTaxIndicator,
         receiveDate = entity.receiveDate,
         location = SimpleLegacyIdentifiableDTO(entity.location?.myId())
      )

   override fun myId(): UUID? = id
}
