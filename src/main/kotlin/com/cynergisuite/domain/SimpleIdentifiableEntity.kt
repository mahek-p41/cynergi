package com.cynergisuite.domain

data class SimpleIdentifiableEntity(
   private val id: Long
) : Identifiable {
   constructor(identifiableEntity: Identifiable) :
      this(id = identifiableEntity.myId()!!)

   override fun myId(): Long? = id

   override fun toString(): String {
      return "$id"
   }
}
