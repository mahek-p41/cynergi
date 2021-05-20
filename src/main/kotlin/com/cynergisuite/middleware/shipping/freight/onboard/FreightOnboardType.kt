package com.cynergisuite.middleware.shipping.freight.onboard

import com.cynergisuite.domain.TypeDomainEntity

data class FreightOnboardType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<FreightOnboardType> {

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
