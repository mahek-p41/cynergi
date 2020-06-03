package com.cynergisuite.middleware.shipping.freight.calc.method

import com.cynergisuite.domain.TypeDomainEntity

data class FreightCalcMethodType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<FreightCalcMethodType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
