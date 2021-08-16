package com.cynergisuite.middleware.location

import com.cynergisuite.middleware.store.Store
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity("fastinfo_prod_import.store_vw")
data class LocationEntity(

   @field:Id
   @field:GeneratedValue
   val id: Long,
   val number: Int,
   val name: String,

) : Location {

   constructor(store: Store) :
      this(
         id = store.myId(),
         number = store.myNumber(),
         name = store.myName(),
      )

   override fun myId(): Long = id
   override fun myNumber(): Int = number
   override fun myName(): String = name
}
