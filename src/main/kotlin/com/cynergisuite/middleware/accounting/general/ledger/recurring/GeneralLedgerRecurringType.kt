package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.domain.TypeDomainEntity

data class GeneralLedgerRecurringType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<GeneralLedgerRecurringType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
