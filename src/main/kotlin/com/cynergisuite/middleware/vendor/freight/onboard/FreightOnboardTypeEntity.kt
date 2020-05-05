package com.cynergisuite.middleware.vendor.freight.onboard

import com.cynergisuite.domain.TypeDomainEntity

data class FreightOnboardTypeEntity(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<FreightOnboardTypeEntity> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
