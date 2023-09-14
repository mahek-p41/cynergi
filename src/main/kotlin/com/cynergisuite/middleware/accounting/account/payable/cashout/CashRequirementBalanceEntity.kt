package com.cynergisuite.middleware.accounting.account.payable.cashout

import java.math.BigDecimal

data class CashRequirementBalanceEntity(
   var weekOnePaid: BigDecimal = BigDecimal.ZERO,
   var weekOneDue: BigDecimal = BigDecimal.ZERO,
   var weekTwoPaid: BigDecimal = BigDecimal.ZERO,
   var weekTwoDue: BigDecimal = BigDecimal.ZERO,
   var weekThreePaid: BigDecimal = BigDecimal.ZERO,
   var weekThreeDue: BigDecimal = BigDecimal.ZERO,
   var weekFourPaid: BigDecimal = BigDecimal.ZERO,
   var weekFourDue: BigDecimal = BigDecimal.ZERO,
   var weekFivePaid: BigDecimal = BigDecimal.ZERO,
   var weekFiveDue: BigDecimal = BigDecimal.ZERO
)
