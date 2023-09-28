package com.cynergisuite.domain

import io.micronaut.core.annotation.Introspected
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull

@Introspected
@Schema(name = "Identifiable", title = "Provides a reference to another model", description = "Describes a simple object that has an ID.  This is usually used as a stand-in when a dependent has a larger more complex 'parent' object when just the ID is required to identify the dependency")
data class SimpleIdentifiableDTO(

   @field:NotNull
   @field:Schema(name = "id", description = "System managed ID that points to a valid instance")
   var id: UUID? = null

) : Identifiable {

   @field:Schema(name = "number", description = "Number as an optional property provided where needed", required = false)
   var number: Long? = null

   @field:Schema(name = "name", description = "Name as an optional property provided where needed", required = false)
   var name: String? = null

   constructor(identifiable: Identifiable) :
      this(
         id = identifiable.myId()
      )

   constructor(id: UUID, number: Long?, name: String?) :
      this(id) {
      this.number = number
      this.name = name
   }

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
