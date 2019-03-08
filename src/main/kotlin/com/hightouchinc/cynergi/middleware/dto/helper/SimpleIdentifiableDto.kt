package com.hightouchinc.cynergi.middleware.dto.helper

import com.fasterxml.jackson.annotation.JsonIgnore
import com.hightouchinc.cynergi.middleware.dto.DataTransferObject
import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto
import com.hightouchinc.cynergi.middleware.entity.IdentifiableEntity

@DataTransferObject
data class SimpleIdentifiableDto(
   val id: Long
) : IdentifiableDto {
   constructor(identifiableEntity: IdentifiableEntity) :
      this(id = identifiableEntity.entityId()!!)

   @JsonIgnore
   override fun dtoId(): Long? = id
}
