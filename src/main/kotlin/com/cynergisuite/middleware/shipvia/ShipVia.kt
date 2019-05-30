package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.Entity
import java.time.OffsetDateTime
import java.util.UUID

data class ShipVia(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val shipViaName: String,
   val shipViaDescription: String
) : Entity<ShipVia> {

   constructor(name: String, description: String):
      this(
         id = null,
         shipViaName = name,
         shipViaDescription = description
      )

   constructor(vo: ShipViaValueObject) :
      this(
         id = vo.id,
         shipViaName = vo.shipViaName!!,
         shipViaDescription = vo.shipViaDescription!!
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): ShipVia = copy()
}



