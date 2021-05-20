package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.domain.TypeDomainEntity

data class RebateType(
   val id: Int,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<RebateType> {

   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
