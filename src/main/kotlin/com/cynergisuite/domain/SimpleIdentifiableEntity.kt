package com.cynergisuite.domain

import java.util.UUID

data class SimpleIdentifiableEntity(
   private val id: UUID
) : Identifiable {
   constructor(identifiableEntity: Identifiable) :
      this(id = identifiableEntity.myId()!!)

   override fun myId(): UUID = id

   override fun toString(): String {
      return "$id"
   }
}
