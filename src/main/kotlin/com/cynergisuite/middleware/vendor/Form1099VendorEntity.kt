package com.cynergisuite.middleware.vendor

import com.cynergisuite.middleware.accounting.account.payable.cashflow.CashFlowReportInvoiceDetailEntity
import com.cynergisuite.middleware.address.AddressEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class Form1099VendorEntity(
   val id: UUID,
   val vendorName: String, // 30 max
   val vendorNumber: Int?,
   val vendorAddress: AddressEntity?,
   val companyAddress: AddressEntity?,
   val federalIdNumber: String?, // 12 max
   val form1099Field: Int,
   val apPaymentPaymentDate: LocalDate,
   val accountName: String,
   val accountNumber: String?,
   val distributionAmount: BigDecimal? = null,
   val isActive: Boolean = true,
   val invoices: MutableSet<Form1099ReportInvoiceDetailEntity>? = LinkedHashSet(),
   var vendorTotals: Form1099TotalsEntity = Form1099TotalsEntity()
)
