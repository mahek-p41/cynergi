package com.cynergisuite.middleware.dto.helper

import com.fasterxml.jackson.annotation.JsonIgnore
import com.cynergisuite.middleware.dto.DataTransferObject
import com.cynergisuite.middleware.dto.IdentifiableDto
import com.cynergisuite.middleware.entity.IdentifiableEntity

@DataTransferObject
data class SimpleIdentifiableDto(
   val id: Long
) : IdentifiableDto {
   constructor(identifiableEntity: IdentifiableEntity) :
      this(id = identifiableEntity.entityId()!!)

   @JsonIgnore
   override fun dtoId(): Long? = id
}
