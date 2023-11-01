package com.cynergisuite.middleware.accounting.account.payable.cashout

import java.util.UUID

data class CashRequirementVendorEntity (
   val vendorCompanyId: UUID,
   val vendorNumber: Int,
   val vendorName: String,
   val invoices: MutableSet<CashRequirementReportInvoiceDetailEntity>? = LinkedHashSet(),
   var vendorTotals: CashRequirementBalanceEntity = CashRequirementBalanceEntity()
)
