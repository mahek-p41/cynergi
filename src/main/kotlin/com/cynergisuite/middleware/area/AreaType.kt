package com.cynergisuite.middleware.area

import com.cynergisuite.domain.TypeDomainEntity

data class AreaType(
   val id: Long,
   val value: String,
   val description: String,
   val localizationCode: String,
   val enabled: Boolean = false,
   val menus: MutableList<MenuType> = mutableListOf()
) : TypeDomainEntity<AreaType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
