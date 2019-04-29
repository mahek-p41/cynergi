package com.cynergisuite.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.cynergisuite.domain.ValueObject
import com.cynergisuite.domain.IdentifiableValueObject
import com.cynergisuite.domain.IdentifiableEntity

@ValueObject
data class SimpleIdentifiableDto(
   val id: Long
) : IdentifiableValueObject {
   constructor(identifiableEntity: IdentifiableEntity) :
      this(id = identifiableEntity.entityId()!!)

   @JsonIgnore
   override fun dtoId(): Long? = id
}
