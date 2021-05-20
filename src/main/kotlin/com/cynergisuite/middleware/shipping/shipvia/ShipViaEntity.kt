package com.cynergisuite.middleware.shipping.shipvia

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.Company
import java.util.UUID

data class ShipViaEntity(
   val id: UUID? = null,
   val description: String,
   val number: Int? = null,
   val company: Company
) : Identifiable {

   constructor(vo: ShipViaDTO, company: Company) :
      this(
         id = vo.id,
         description = vo.description!!,
         number = vo.number,
         company = company
      )

   override fun myId(): UUID? = id
}
