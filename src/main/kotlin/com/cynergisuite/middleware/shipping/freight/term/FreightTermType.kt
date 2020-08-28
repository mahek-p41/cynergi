package com.cynergisuite.middleware.shipping.freight.term

import com.cynergisuite.domain.TypeDomainEntity

data class FreightTermType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<FreightTermType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
