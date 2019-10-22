package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@DataTransferObject
@Schema(name = "IdentifiableDataTransferObject", title = "Provides a reference to another model", description = "Describes a simple object that has an ID.  This is usually used as a stand-in when a dependent has a larger more complex 'parent' object when just the ID is required to identify the dependency")
data class SimpleIdentifiableDataTransferObject(

   @field:NotNull
   @field:Positive
   @field:Schema(name = "id", description = "System managed ID that points to a valid instance")
   var id: Long? = null

): IdentifiableDataTransferObject {
   override fun dtoId(): Long? = id
}
