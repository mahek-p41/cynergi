package com.cynergisuite.middleware.inventory.location

import com.cynergisuite.domain.TypeDomain

data class InventoryLocationType(
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
