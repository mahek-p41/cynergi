package com.cynergisuite.middleware.area

import com.cynergisuite.domain.Identifiable

import com.cynergisuite.middleware.company.Company

data class AreaEntity(
   val id: Long? = null,
   val areaType: AreaType,
   val company: Company
) : Identifiable {

   override fun myId(): Long? = id
}
