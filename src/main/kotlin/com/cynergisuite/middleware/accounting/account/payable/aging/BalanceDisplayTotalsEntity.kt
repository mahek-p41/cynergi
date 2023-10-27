package com.cynergisuite.middleware.accounting.account.payable.aging

import java.math.BigDecimal

data class BalanceDisplayTotalsEntity(
   var balanceTotal: BigDecimal = BigDecimal.ZERO,
   var currentTotal: BigDecimal = BigDecimal.ZERO,
   var oneToThirtyTotal: BigDecimal = BigDecimal.ZERO,
   var thirtyOneToSixtyTotal: BigDecimal = BigDecimal.ZERO,
   var overSixtyTotal: BigDecimal = BigDecimal.ZERO
)
