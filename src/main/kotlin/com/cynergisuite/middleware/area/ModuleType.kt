package com.cynergisuite.middleware.area

import com.cynergisuite.domain.TypeDomainEntity

data class ModuleType(
   val id: Long,
   val value: String,
   val program: String? = null,
   val description: String,
   val localizationCode: String,
   val level: Int? = null,
   val menuType: MenuType? = null
) : TypeDomainEntity<ModuleType> {

   override fun myId(): Long = id
   override fun myValue(): String = value
   override fun myDescription(): String = description
   override fun myLocalizationCode(): String = localizationCode
}
