package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.Entity
import java.time.OffsetDateTime
import java.util.UUID

data class ShipVia(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val name: String,
   val description: String
) : Entity<ShipVia> {

   constructor(name: String, description: String):
      this(
         id = null,
         name = name,
         description = description
      )

   constructor(vo: ShipViaValueObject) :
      this(
         id = vo.id,
         name = vo.name!!,
         description = vo.description!!
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): ShipVia = copy()
}



