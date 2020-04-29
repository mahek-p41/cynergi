package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.domain.TypeDomainEntity

data class BankCurrencyType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<BankCurrencyType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
