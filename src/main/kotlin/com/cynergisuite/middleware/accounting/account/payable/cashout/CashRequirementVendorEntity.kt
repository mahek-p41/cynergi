package com.cynergisuite.middleware.accounting.account.payable.cashout

import java.util.*
import kotlin.collections.LinkedHashSet

data class CashRequirementVendorEntity (
   val vendorCompanyId: UUID,
   val vendorNumber: Int,
   val vendorName: String,
   val invoices: MutableSet<CashRequirementReportInvoiceDetailEntity>? = LinkedHashSet(),
   var vendorTotals: CashRequirementBalanceEntity = CashRequirementBalanceEntity()
)
