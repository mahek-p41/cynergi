package com.cynergisuite.middleware.shipping.shipvia

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.company.CompanyEntity
import java.util.UUID

data class ShipViaEntity(
   val id: UUID? = null,
   val description: String,
   val number: Int? = null,
   val company: CompanyEntity
) : Identifiable {

   constructor(vo: ShipViaDTO, company: CompanyEntity) :
      this(
         id = vo.id,
         description = vo.description!!,
         number = vo.number,
         company = company
      )

   override fun myId(): UUID? = id
}
