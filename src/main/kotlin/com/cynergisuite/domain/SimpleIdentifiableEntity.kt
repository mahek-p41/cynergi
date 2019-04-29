package com.cynergisuite.domain

import com.cynergisuite.domain.IdentifiableValueObject
import com.cynergisuite.domain.IdentifiableEntity

data class SimpleIdentifiableEntity(
   private val id: Long
) : IdentifiableEntity {
   constructor(identifiableEntity: IdentifiableEntity) :
      this(id = identifiableEntity.entityId()!!)

   constructor(identifiableDto: IdentifiableValueObject):
      this(id = identifiableDto.dtoId()!!)

   override fun entityId(): Long? = id

   override fun toString(): String {
      return "$id"
   }
}
