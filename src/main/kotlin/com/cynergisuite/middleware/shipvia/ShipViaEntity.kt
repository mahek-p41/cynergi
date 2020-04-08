package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company

data class ShipViaEntity(
   val id: Long? = null,
   val description: String,
   val number: Int? = null,
   val company: Company
) : Identifiable {

   constructor(vo: ShipViaValueObject, company: Company) :
      this(
         id = vo.id,
         description = vo.description!!,
         number = vo.number,
         company = company
      )

   override fun myId(): Long? = id
}



