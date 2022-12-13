package com.cynergisuite.middleware.accounting.account.payable.cashflow

import java.util.UUID
import kotlin.collections.LinkedHashSet

data class CashFlowVendorEntity (
   val vendorCompanyId: UUID,
   val vendorNumber: Int,
   val vendorName: String,
   var invoices: MutableSet<CashFlowReportInvoiceDetailEntity>? = LinkedHashSet(),
   var vendorTotals: CashFlowBalanceEntity = CashFlowBalanceEntity()
   )
