package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconSummaryEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationEntity

data class BankReconReportEntity(
   val vendors: MutableList<BankReconciliationEntity>?,
   val reconciliationSummaries: BankReconSummaryEntity
)
