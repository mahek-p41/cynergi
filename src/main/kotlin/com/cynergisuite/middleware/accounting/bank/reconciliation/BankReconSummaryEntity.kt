package com.cynergisuite.middleware.accounting.bank.reconciliation

import java.math.BigDecimal

data class BankReconSummaryEntity(
   var ach: BigDecimal = BigDecimal.ZERO,
   var check: BigDecimal = BigDecimal.ZERO,
   var deposit: BigDecimal = BigDecimal.ZERO,
   var fee: BigDecimal = BigDecimal.ZERO,
   var interest: BigDecimal = BigDecimal.ZERO,
   var misc: BigDecimal = BigDecimal.ZERO,
   var serviceCharge: BigDecimal = BigDecimal.ZERO,
   var transfer: BigDecimal = BigDecimal.ZERO,
   var returnCheck: BigDecimal = BigDecimal.ZERO,
   var void: BigDecimal = BigDecimal.ZERO
)
