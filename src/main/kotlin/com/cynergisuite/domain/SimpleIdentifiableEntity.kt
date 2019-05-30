package com.cynergisuite.domain

data class SimpleIdentifiableEntity(
   private val id: Long
) : IdentifiableEntity {
   constructor(identifiableEntity: IdentifiableEntity) :
      this(id = identifiableEntity.entityId()!!)

   constructor(identifiableDto: IdentifiableValueObject):
      this(id = identifiableDto.valueObjectId()!!)

   override fun entityId(): Long? = id

   override fun toString(): String {
      return "$id"
   }
}
