package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.Entity
import java.time.OffsetDateTime
import java.util.UUID

data class ShipViaEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val description: String
) : Entity<ShipViaEntity> {

   constructor(description: String):
      this(
         id = null,
         description = description
      )

   constructor(vo: ShipViaValueObject) :
      this(
         id = vo.id,
         description = vo.description!!
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): ShipViaEntity = copy()
}



