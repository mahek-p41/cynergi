package com.cynergisuite.middleware.accounting.account.payable.invoice

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceSelectedType
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceType
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.vendor.VendorEntity
import java.math.BigDecimal
import java.time.LocalDate

data class AccountPayableInvoiceEntity(
   val id: Long? = null,
   val vendor: VendorEntity,
   val invoice: String,
   val purchaseOrder: Identifiable?,
   val invoiceDate: LocalDate,
   val invoiceAmount: BigDecimal,
   val discountAmount: BigDecimal?,
   val discountPercent: BigDecimal?,
   val autoDistributionApplied: Boolean,
   val discountTaken: BigDecimal?,
   val entryDate: LocalDate,
   val expenseDate: LocalDate,
   val discountDate: LocalDate?,
   val employee: EmployeeEntity,
   val originalInvoiceAmount: BigDecimal,
   val message: String?,
   val selected: AccountPayableInvoiceSelectedType,
   val multiplePaymentIndicator: Boolean,
   val paidAmount: BigDecimal,
   val selectedAmount: BigDecimal?,
   val type: AccountPayableInvoiceType,
   val status: AccountPayableInvoiceStatusType,
   val dueDate: LocalDate,
   val payTo: Identifiable,
   val separateCheckIndicator: Boolean,
   val useTaxIndicator: Boolean,
   val receiveDate: LocalDate?,
   val location: Identifiable?
) : Identifiable {

   constructor(
      dto: AccountPayableInvoiceDTO,
      vendor: VendorEntity,
      purchaseOrder: SimpleIdentifiableEntity?,
      employeeNumber: EmployeeEntity,
      selected: AccountPayableInvoiceSelectedType,
      type: AccountPayableInvoiceType,
      status: AccountPayableInvoiceStatusType,
      payTo: SimpleIdentifiableEntity,
      location: SimpleIdentifiableEntity?
   ) :
      this(
         id = dto.id,
         vendor = vendor,
         invoice = dto.invoice!!,
         purchaseOrder = purchaseOrder,
         invoiceDate = dto.invoiceDate!!,
         invoiceAmount = dto.invoiceAmount!!,
         discountAmount = dto.discountAmount,
         discountPercent = dto.discountPercent,
         autoDistributionApplied = dto.autoDistributionApplied!!,
         discountTaken = dto.discountTaken,
         entryDate = dto.entryDate!!,
         expenseDate = dto.expenseDate!!,
         discountDate = dto.discountDate,
         employee = employeeNumber,
         originalInvoiceAmount = dto.originalInvoiceAmount!!,
         message = dto.message,
         selected = selected,
         multiplePaymentIndicator = dto.multiplePaymentIndicator!!,
         paidAmount = dto.paidAmount!!,
         selectedAmount = dto.selectedAmount,
         type = type,
         status = status,
         dueDate = dto.dueDate!!,
         payTo = payTo,
         separateCheckIndicator = dto.separateCheckIndicator!!,
         useTaxIndicator = dto.useTaxIndicator!!,
         receiveDate = dto.receiveDate,
         location = location
      )

   override fun myId(): Long? = id
}
