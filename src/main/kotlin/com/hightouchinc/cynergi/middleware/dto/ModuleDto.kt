package com.hightouchinc.cynergi.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.entity.Module
import com.hightouchinc.cynergi.middleware.localization.MessageCodes
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ModuleDto (

   @field:Positive(message = MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:NotNull(message = MessageCodes.Validation.NOT_NULL)
   @field:Size(message = MessageCodes.Validation.SIZE, min = 6, max = 6)
   var name: String? = null,

   @field:NotNull(message = MessageCodes.Validation.NOT_NULL)
   @field:Size(message = MessageCodes.Validation.SIZE, min = 6, max = 6)
   var literal: String? = null

) : DataTransferObjectBase<ModuleDto>() {

   constructor(entity: Module) :
      this(
         id = entity.id,
         name = entity.name,
         literal = entity.literal
      )

   override fun dtoId(): Long? = id
   override fun copyMe(): ModuleDto = copy()
}

@DataTransferObject
data class ModuleTreeDto (
   val id: Long,
   val name: String,
   val literal: String
) {
   constructor(module: Module) :
      this(
         id = module.id!!,
         name = module.name,
         literal = module.literal
      )
}
