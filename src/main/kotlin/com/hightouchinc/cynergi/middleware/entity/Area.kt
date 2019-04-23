package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.dto.AreaDto
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import java.time.OffsetDateTime
import java.util.UUID

data class Area (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val company: IdentifiableEntity,
   val menu: IdentifiableEntity,
   val level: Int
) : Entity<Area> {

   constructor(company: Company, menu: Menu, level: Int) :
      this(
         id = null,
         company = company,
         menu = menu,
         level = level
      )

   constructor(dto: AreaDto, companyId: Long) :
      this(
         id = dto.id,
         company = SimpleIdentifiableEntity(id = companyId),
         menu = SimpleIdentifiableEntity(dto.menu!!),
         level = dto.level!!
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Area = copy()
}
