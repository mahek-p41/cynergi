package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconSummaryEntity
import com.cynergisuite.middleware.accounting.bank.reconciliation.BankReconciliationReportDetailEntity

data class BankReconciliationReportEntity(
   val vendors: MutableList<BankReconciliationReportDetailEntity>?,
   val reconciliationSummaries: BankReconSummaryEntity
)
