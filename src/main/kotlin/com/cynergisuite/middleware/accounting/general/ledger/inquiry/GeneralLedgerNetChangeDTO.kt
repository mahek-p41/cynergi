package com.cynergisuite.middleware.accounting.general.ledger.inquiry

import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal

@Introspected
data class GeneralLedgerNetChangeDTO(
   val debit: BigDecimal,
   val credit: BigDecimal,
   val netActivityPeriod: List<BigDecimal?>,
   var beginBalance: BigDecimal,
   val endBalance: BigDecimal,
   val netChange: BigDecimal,
)
