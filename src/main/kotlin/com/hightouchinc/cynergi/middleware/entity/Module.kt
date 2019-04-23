package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.dto.ModuleDto
import java.time.OffsetDateTime
import java.util.UUID

data class Module (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val name: String,
   val literal: String,
   val menu: IdentifiableEntity
) : Entity<Module> {

   constructor(name: String, literal: String, menu: IdentifiableEntity) :
      this(
         id = null,
         name = name,
         literal = literal,
         menu = menu
      )

   constructor(dto: ModuleDto, menu: Menu) :
      this(
         id = dto.id,
         name = dto.name!!,
         literal = dto.literal!!,
         menu = menu
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Module = copy()
   override fun hashCode(): Int = uuRowId.hashCode()
}

