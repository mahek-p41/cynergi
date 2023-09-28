package com.cynergisuite.middleware.accounting.account.payable.cashflow

data class AccountPayableCashFlowEntity(
   val vendors: MutableList<CashFlowVendorEntity>?,
   val cashflowTotals: CashFlowBalanceEntity
)
