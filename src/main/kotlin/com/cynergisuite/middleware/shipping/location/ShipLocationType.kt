package com.cynergisuite.middleware.shipping.location

import com.cynergisuite.domain.TypeDomainEntity

data class ShipLocationType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String
) : TypeDomainEntity<ShipLocationType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
