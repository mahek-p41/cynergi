package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(name = "SimpleIdentifiable", title = "Provides a reference to another model", description = "Describes a simple object that has an ID.  This is usually used as a stand-in when a dependent has a larger more complex 'parent' object when just the ID is required to identify the dependency")
data class SimpleIdentifiableDataTransferObject(

   @field:NotNull
   @field:Positive
   @field:Schema(name = "id", description = "System managed ID that points to a valid instance")
   var id: Long? = null

): Identifiable {

   constructor(identifiable: Identifiable):
      this(
         id = identifiable.myId()
      )

   override fun myId(): Long? = id

   override fun hashCode(): Int = id.hashCode()
   override fun equals(other: Any?): Boolean {
      return if (other is SimpleIdentifiableDataTransferObject) {
         other.id == this.id
      } else {
         false
      }
   }
}
