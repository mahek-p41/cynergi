package com.cynergisuite.middleware.accounting.account.payable.aging

import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class AgingReportInvoiceDetailEntity(
   val invoiceCompanyId: UUID,
   val invoiceVendorId: UUID,
   val invoice: String,
   val invoiceDate: LocalDate,
   val invoiceAmount: BigDecimal,
   var invoiceDiscountAmount: BigDecimal?,
   val invoiceExpenseDate: LocalDate,
   var invoicePaidAmount: BigDecimal,
   var invoiceStatus: AccountPayableInvoiceStatusType,
   val invoiceDueDate: LocalDate,
   val apPaymentPaymentDate: LocalDate?,
   val apPaymentDateVoided: LocalDate?,
   val apPaymentDetailAmount: BigDecimal?,
   var balance: BigDecimal,
   var balanceDisplay: BalanceDisplayEnum?
)
