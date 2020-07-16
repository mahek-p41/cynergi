package com.cynergisuite.middleware.area

import com.cynergisuite.domain.Identifiable

import com.cynergisuite.middleware.company.Company

data class ModuleEntity(
   val id: Long? = null,
   val moduleType: ModuleType,
   val level: Int,
   val company: Company
) : Identifiable {

   override fun myId(): Long? = id
}
