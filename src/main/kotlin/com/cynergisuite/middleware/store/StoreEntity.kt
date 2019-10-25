package com.cynergisuite.middleware.store

import com.cynergisuite.domain.Identifiable
import java.time.OffsetDateTime

data class StoreEntity(
   val id: Long,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val number: Int,
   val name: String,
   val dataset: String
) : Identifiable {

   constructor(store: StoreValueObject) :
      this(
         id = store.id,
         number = store.number!!,
         name = store.name!!,
         dataset = store.dataset!!
      )

   override fun myId(): Long? = id
}
