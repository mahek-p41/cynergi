package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Positive

data class NotificationDomainType (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val value: String,
   val description: String
) : Entity<NotificationDomainType> {

   constructor(dto: NotificationDomainTypeDto) :
      this(
         id = dto.id,
         value = dto.value,
         description = dto.description
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): NotificationDomainType = copy()
}

@JsonInclude(NON_NULL)
data class NotificationDomainTypeDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   val value: String,

   val description: String

) : DataTransferObjectBase<NotificationDomainTypeDto>() {

   constructor(entity: NotificationDomainType) :
      this(
         id = entity.id,
         value = entity.value,
         description = entity.description
      )

   override fun dtoId(): Long? = id

   override fun copyMe(): NotificationDomainTypeDto = copy()
}
