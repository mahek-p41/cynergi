package com.cynergisuite.middleware.accounting.account.payable.cashflow

import java.math.BigDecimal
import java.time.LocalDate

data class CashFlowBalanceEntity(
   var dateOneAmount: BigDecimal = BigDecimal.ZERO,
   var dateTwoAmount: BigDecimal = BigDecimal.ZERO,
   var dateThreeAmount: BigDecimal = BigDecimal.ZERO,
   var dateFourAmount: BigDecimal = BigDecimal.ZERO,
   var dateFiveAmount: BigDecimal = BigDecimal.ZERO,
   var discountTaken: BigDecimal = BigDecimal.ZERO,
   var discountLost: BigDecimal = BigDecimal.ZERO,
   var discountDate: LocalDate? = null
)
