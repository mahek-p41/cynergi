package com.cynergisuite.middleware.accounting.account.payable.cashout

data class AccountPayableCashRequirementEntity(
   val vendors: MutableList<CashRequirementVendorEntity>?,
   val cashoutTotals: CashRequirementBalanceEntity
)
