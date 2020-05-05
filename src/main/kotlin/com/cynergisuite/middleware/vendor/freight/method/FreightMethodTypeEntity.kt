package com.cynergisuite.middleware.vendor.freight.method

import com.cynergisuite.domain.TypeDomainEntity

data class FreightMethodTypeEntity(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<FreightMethodTypeEntity> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
