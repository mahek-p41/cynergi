package com.cynergisuite.middleware.accounting.account.payable.aging

import java.util.*
import kotlin.collections.LinkedHashSet

data class AgingReportVendorDetailEntity(
   val vendorCompanyId: UUID,
   val vendorNumber: Int,
   val vendorName: String,
   val invoices: MutableSet<AgingReportInvoiceDetailEntity>? = LinkedHashSet(),
   var vendorTotals: BalanceDisplayTotalsEntity = BalanceDisplayTotalsEntity()
) {}
