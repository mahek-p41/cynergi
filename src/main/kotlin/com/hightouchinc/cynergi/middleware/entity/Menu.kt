package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.dto.MenuDto
import java.time.OffsetDateTime
import java.util.UUID

data class Menu (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val name: String,
   val literal: String
) : Entity<Menu> {

   constructor(name: String, literal: String) :
      this(
         id = null,
         name = name,
         literal = literal
      )

   constructor(dto: MenuDto) :
      this(
         id = dto.id,
         name = dto.name!!,
         literal = dto.literal!!
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Menu = copy()
}

data class MenuTree (
   val id: Long,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val name: String,
   val literal: String,
   val modules: MutableSet<Module>
) {
   fun rowId(): UUID = uuRowId
   fun copyMe(): MenuTree = copy()
   fun entityId(): Long = id
   override fun hashCode(): Int = uuRowId.hashCode()
   override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as MenuTree

      if (uuRowId != other.uuRowId) return false

      return true
   }
}

