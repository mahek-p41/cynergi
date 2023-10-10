package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.domain.TypeDomain
import io.micronaut.core.annotation.Introspected

@Introspected
data class GeneralLedgerRecurringType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomain() {

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}