package com.cynergisuite.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@ValueObject
@Schema(name = "Identifiable", title = "Provides a reference to another model", description = "Describes a simple object that has an ID.  This is usually used as a stand-in when a dependent has a larger more complex 'parent' object when just the ID is required to identify the dependency")
data class SimpleIdentifiableValueObject(

   @field:NotNull
   @field:Schema(name = "id", description = "The system generated ID (aka primary key) for the associated item", required = true)
   var id: Long? = null

) : IdentifiableValueObject {

   constructor(identifiableEntity: IdentifiableEntity) :
      this(
         id = identifiableEntity.entityId()
      )

   constructor(identifiableValueObject: IdentifiableValueObject) :
      this (
         id = identifiableValueObject.valueObjectId()
      )

   @JsonIgnore
   override fun valueObjectId(): Long? = id
}
