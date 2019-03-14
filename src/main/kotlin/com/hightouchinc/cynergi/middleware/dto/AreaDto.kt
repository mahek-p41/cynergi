package com.hightouchinc.cynergi.middleware.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.dto.helper.SimpleIdentifiableDto
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.entity.Area
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Validation.MAX
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Validation.MIN
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Validation.NOT_NULL
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@JsonInclude(NON_NULL)
data class AreaDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:NotNull(message = NOT_NULL)
   var menu: IdentifiableDto? = null,

   @field:NotNull(message = NOT_NULL)
   @field:Min(1, message = MIN)
   @field:Max(99, message = MAX)
   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var level: Int? = null

) : DataTransferObjectBase<AreaDto>() {

   constructor(entity: Area) :
      this(
         id = entity.id,
         menu = SimpleIdentifiableDto(entity.menu),
         level = entity.level
      )

   override fun dtoId(): Long? = id
   override fun copyMe(): AreaDto = copy()
}
