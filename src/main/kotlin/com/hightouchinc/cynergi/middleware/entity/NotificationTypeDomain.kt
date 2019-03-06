package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Positive

data class NotificationTypeDomain (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val value: String,
   val description: String
) : TypeDomainEntity<NotificationTypeDomain> {

   constructor(id: Long, value: String, description: String) :
      this(
         id = id,
         uuRowId = UUID.randomUUID(),
         timeCreated = OffsetDateTime.now(),
         timeUpdated = OffsetDateTime.now(),
         value = value,
         description = description
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): NotificationTypeDomain = copy()

   override fun myValue(): String = value

   override fun myDescription(): String = description
}

@JsonInclude(NON_NULL)
data class NotificationTypeDomainDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   val value: String,

   val description: String

) : DataTransferObjectBase<NotificationTypeDomainDto>() {

   constructor(entity: NotificationTypeDomain) :
      this(
         id = entity.id,
         value = entity.value,
         description = entity.description
      )

   override fun dtoId(): Long? = id

   override fun copyMe(): NotificationTypeDomainDto = copy()
}
