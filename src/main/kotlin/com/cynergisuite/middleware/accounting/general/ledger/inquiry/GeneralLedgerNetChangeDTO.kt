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
   var ytdDebit: BigDecimal = BigDecimal.ZERO,
   var ytdCredit: BigDecimal = BigDecimal.ZERO,
   var beginBalance2: BigDecimal? = null,
) {
   operator fun plus(other: GeneralLedgerNetChangeDTO): GeneralLedgerNetChangeDTO {
      return GeneralLedgerNetChangeDTO(
         debit  = debit.plus(other.debit),
         credit  = credit.plus(other.credit),
         beginBalance  = beginBalance.plus(other.beginBalance),
         endBalance  = endBalance.plus(other.endBalance),
         netChange  = netChange.plus(other.netChange),
         ytdDebit  = other.ytdDebit.let { ytdDebit.plus(it) },
         ytdCredit  = other.ytdCredit.let { ytdCredit.plus(it) },
      )
   }
}
