package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.company.Company
import java.time.OffsetDateTime
import java.util.UUID

data class ShipViaEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val description: String,
   val number: Int,
   val company: Company
) : Entity<ShipViaEntity> {

   constructor(vo: ShipViaValueObject, company: Company) :
      this(
         id = vo.id,
         description = vo.description!!,
         number = vo.number!!,
         company = company
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): ShipViaEntity = copy()
}



