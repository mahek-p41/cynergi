package com.cynergisuite.middleware.accounting.account.payable.aging

data class AccountPayableAgingReportEntity(
   val vendors: MutableList<AgingReportVendorDetailEntity>?,
   val agedTotals: BalanceDisplayTotalsEntity
) {}
