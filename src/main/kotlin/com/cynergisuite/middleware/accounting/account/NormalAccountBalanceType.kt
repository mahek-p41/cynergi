package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.domain.TypeDomainEntity

data class NormalAccountBalanceType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<NormalAccountBalanceType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
