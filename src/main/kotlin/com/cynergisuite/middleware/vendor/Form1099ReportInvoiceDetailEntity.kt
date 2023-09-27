package com.cynergisuite.middleware.vendor

import com.cynergisuite.middleware.accounting.account.payable.AccountPayableInvoiceStatusType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class Form1099ReportInvoiceDetailEntity (

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
   val invoiceDueDate: LocalDate,
   var discountAmount: BigDecimal?,
   var lostAmount: BigDecimal?,
   var balance: BigDecimal,
   var fieldNumber: Int,
   var distributionAmount: BigDecimal
)
