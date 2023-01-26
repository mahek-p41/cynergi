package com.cynergisuite.middleware.accounting.account.payable.cashout

import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.payment.AccountPayablePaymentStatusType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class CashRequirementReportInvoiceDetailEntity(
   val invoiceCompanyId: UUID,
   val invoiceVendorId: UUID,
   val invoice: String,
   val invoiceDate: LocalDate,
   val invoiceAmount: BigDecimal,
   var invoiceDiscountAmount: BigDecimal?,
   val invoiceExpenseDate: LocalDate,
   var invoicePaidAmount: BigDecimal,
   var invoiceDiscountTaken: BigDecimal,
   var invoiceStatus: AccountPayableInvoiceStatusType,
   val invoiceDueDate: LocalDate,
   val apPaymentPaymentDate: LocalDate?,
   val apPaymentStatusId: Int?,
   val apPaymentStatusValue: String?,
   val apPaymentDateVoided: LocalDate?,
   val apPaymentIsVoided: Boolean,
   val apPaymentDetailAmount: BigDecimal?,
   var balance: BigDecimal,
   var balanceDisplay: CashRequirementBalanceEnum?
)
