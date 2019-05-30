package com.cynergisuite.domain

import com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import com.fasterxml.jackson.annotation.JsonIgnore
import javax.validation.constraints.NotNull

@ValueObject
data class SimpleIdentifiableValueObject(

   @field:NotNull(message = NOT_NULL)
   var id: Long? = null
) : IdentifiableValueObject {

   constructor(identifiableEntity: IdentifiableEntity) :
      this(
         id = identifiableEntity.entityId()
      )

   @JsonIgnore
   override fun valueObjectId(): Long? = id
}
