package com.cynergisuite.middleware.accounting.general.ledger.inquiry

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal

@Introspected
data class GeneralLedgerNetChangeDTO(
   var debit: BigDecimal,
   var credit: BigDecimal,
   val netActivityPeriod: List<BigDecimal?>,
   var beginBalance: BigDecimal,
   var endBalance: BigDecimal,
   var netChange: BigDecimal,
)
