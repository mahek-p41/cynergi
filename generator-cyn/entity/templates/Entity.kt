package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.POSITIVE_NUMBER_REQUIRED
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Positive

data class <%= entityname %> (
   var id: Long? = null,
   var uuRowId: UUID? = UUID.randomUUID(),
   var timeCreated: OffsetDateTime? = OffsetDateTime.now(),
   var timeUpdated: OffsetDateTime? = timeCreated
) : Entity {

   constructor(dto: <%= entityname %>Dto) :
      this(
         id = dto.id
      )

   override fun entityId(): Long? = id
}

data class <%= entityname %>Dto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null

) : DataTransferObjectBase<ChecklistAutoDto>() {

   constructor(entity: <%= entityname %>) :
      this(
         id = entity.id
      )

   override fun copyMe(): <%= entityname %>Dto = copy()
}
