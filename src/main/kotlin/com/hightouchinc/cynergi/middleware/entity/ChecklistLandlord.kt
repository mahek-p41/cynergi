package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.POSITIVE_NUMBER_REQUIRED
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Positive

data class ChecklistLandlord (
   var id: Long? = null,
   var uuRowId: UUID? = UUID.randomUUID(),
   var timeCreated: OffsetDateTime? = OffsetDateTime.now(),
   var timeUpdated: OffsetDateTime? = timeCreated,
   var address: Boolean?
) : Entity {

   constructor(dto: ChecklistLandlordDto) :
      this(
         id = dto.id,
         address = dto.address
      )

   override fun entityId(): Long? = id
}

data class ChecklistLandlordDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   var address: Boolean? = null

) : DataTransferObjectBase<ChecklistLandlordDto>() {

   constructor(entity: ChecklistLandlord) :
      this(
         id = entity.id,
         address = entity.address
      )

   override fun copyMe(): ChecklistLandlordDto = copy()
}
