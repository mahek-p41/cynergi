package com.cynergisuite.domain

import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull

@Schema(name = "SimpleTypeDomain", description = "Defines a domain value by it's id only")
data class SimpleTypeDomainDTO(

   @field:NotNull
   @field:Schema(name = "id", description = "System managed ID for a domain value", nullable = false)
   var id: Int? = null

) {
   constructor(entity: TypeDomain) :
      this(
         id = entity.myId()
      )
}
