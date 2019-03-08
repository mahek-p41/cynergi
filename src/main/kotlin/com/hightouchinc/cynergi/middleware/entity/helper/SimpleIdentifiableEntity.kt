package com.hightouchinc.cynergi.middleware.entity.helper

import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto
import com.hightouchinc.cynergi.middleware.entity.IdentifiableEntity

data class SimpleIdentifiableEntity(
   private val id: Long
) : IdentifiableEntity {
   constructor(identifiableEntity: IdentifiableEntity) :
      this(id = identifiableEntity.entityId()!!)

   constructor(identifiableDto: IdentifiableDto):
      this(id = identifiableDto.dtoId()!!)

   override fun entityId(): Long? = id

   override fun toString(): String {
      return "$id"
   }
}
