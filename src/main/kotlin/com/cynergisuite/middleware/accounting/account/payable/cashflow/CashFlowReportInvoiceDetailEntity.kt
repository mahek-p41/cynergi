package com.cynergisuite.middleware.accounting.account.payable.cashflow

import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import com.cynergisuite.middleware.accounting.account.payable.cashout.CashRequirementBalanceEnum
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class CashFlowReportInvoiceDetailEntity (

   val invoiceCompanyId: UUID,
   val invoiceVendorId: UUID,
   val invoice: String,
   val invoiceDate: LocalDate,
   val invoiceAmount: BigDecimal,
   var invoiceDiscountAmount: BigDecimal?,
   var invoiceDiscountDate: LocalDate?,
   val invoiceExpenseDate: LocalDate,
   var invoicePaidAmount: BigDecimal,
   var invoiceDiscountTaken: BigDecimal,
   var invoiceDiscountPercent: BigDecimal?,
   var invoiceStatus: AccountPayableInvoiceStatusType,
   val invoiceDueDate: LocalDate,
   var discountAmount: BigDecimal?,
   var lostAmount: BigDecimal?,
   var balance: BigDecimal,
   var balanceDisplay: CashRequirementBalanceEnum?
)
