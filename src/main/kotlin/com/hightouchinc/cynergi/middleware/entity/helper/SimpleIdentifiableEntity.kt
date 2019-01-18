package com.hightouchinc.cynergi.middleware.entity.helper

import com.hightouchinc.cynergi.middleware.entity.IdentifiableEntity

data class SimpleIdentifiableEntity(
   private val id: Long?
) : IdentifiableEntity {
   constructor(identifiableEntity: IdentifiableEntity) :
      this(id = identifiableEntity.entityId())

   override fun entityId(): Long? = id

   override fun toString(): String {
      return "$id"
   }
}
