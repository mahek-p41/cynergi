package com.cynergisuite.middleware.accounting.general.ledger.inquiry

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal

@Introspected
data class GeneralLedgerNetChangeDTO(
   var debit: BigDecimal,
   var credit: BigDecimal,
   var netActivityPeriod: List<BigDecimal?> = mutableListOf(),
   var beginBalance: BigDecimal,
   var endBalance: BigDecimal,
   var netChange: BigDecimal,
) {
   operator fun plusAssign(otherAccountTotals: GeneralLedgerNetChangeDTO) {
      debit = debit.plus(otherAccountTotals.debit)
      credit = credit.plus(otherAccountTotals.credit)
      netActivityPeriod = netActivityPeriod.plus(otherAccountTotals.netActivityPeriod)
      beginBalance = beginBalance.plus(otherAccountTotals.beginBalance)
      endBalance = endBalance.plus(otherAccountTotals.endBalance)
      netChange = netChange.plus(otherAccountTotals.netChange)
   }
}
