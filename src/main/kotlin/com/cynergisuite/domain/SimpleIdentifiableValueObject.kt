package com.cynergisuite.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@ValueObject
@Schema(name = "Identifiable", description = "Describes a simple object that has an ID.  This is usually used as a stand-in when a dependent has a larger more complex 'parent' object when just the ID is required to identify the dependency")
data class SimpleIdentifiableValueObject(

   @field:NotNull
   var id: Long? = null

) : IdentifiableValueObject {

   constructor(identifiableEntity: IdentifiableEntity) :
      this(
         id = identifiableEntity.entityId()
      )

   @JsonIgnore
   override fun valueObjectId(): Long? = id
}
