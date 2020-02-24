package com.cynergisuite.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(name = "Identifiable", title = "Provides a reference to another model", description = "Describes a simple object that has an ID.  This is usually used as a stand-in when a dependent has a larger more complex 'parent' object when just the ID is required to identify the dependency")
data class SimpleIdentifiableValueObject(

   @field:Positive
   @field:NotNull
   @field:Schema(name = "id", description = "The system generated ID (aka primary key) for the associated item", required = true)
   var id: Long? = null

) : Identifiable {

   constructor(identifiableEntity: Identifiable) :
      this(
         id = identifiableEntity.myId()
      )

   @JsonIgnore
   override fun myId(): Long? = id
}
