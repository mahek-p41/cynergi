package com.cynergisuite.middleware.area

import com.cynergisuite.domain.TypeDomainEntity

data class MenuType(
   val id: Int,
   val parentId: Int? = null,
   val value: String,
   val description: String,
   val localizationCode: String,
   val orderNumber: Int,
   val areaType: AreaType? = null,
   val menus: MutableList<MenuType> = mutableListOf(),
   val modules: MutableList<ModuleType> = mutableListOf()
) : TypeDomainEntity<MenuType> {
   override fun myId(): Int = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
