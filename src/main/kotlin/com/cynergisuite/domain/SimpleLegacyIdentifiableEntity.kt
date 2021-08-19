package com.cynergisuite.domain

data class SimpleLegacyIdentifiableEntity(
   private val id: Long
) : LegacyIdentifiable {

   constructor(id: Int) :
      this(id = id.toLong())

   override fun myId(): Long = id
}
