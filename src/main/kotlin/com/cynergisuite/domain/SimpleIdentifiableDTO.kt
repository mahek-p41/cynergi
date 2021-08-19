package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull

@Schema(name = "Identifiable", title = "Provides a reference to another model", description = "Describes a simple object that has an ID.  This is usually used as a stand-in when a dependent has a larger more complex 'parent' object when just the ID is required to identify the dependency")
data class SimpleIdentifiableDTO(

   @field:NotNull
   @field:Schema(name = "id", description = "System managed ID that points to a valid instance")
   var id: UUID? = null

) : Identifiable {

   constructor(identifiable: Identifiable) :
      this(
         id = identifiable.myId()
      )

   override fun myId(): UUID? = id

   override fun hashCode(): Int = id.hashCode()
   override fun equals(other: Any?): Boolean {
      return if (other is SimpleIdentifiableDTO) {
         other.id == this.id
      } else {
         false
      }
   }
}
