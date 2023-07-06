package com.cynergisuite.middleware.accounting.general.ledger.deposit

import com.cynergisuite.domain.TypeDomain

data class StagingDepositType(
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
