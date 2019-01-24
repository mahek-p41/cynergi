package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Positive

enum class NotificationType(
   val value: String
) {
   Store("S"),
   Employee("E"),
   Department("D"),
   All("A");

   companion object {

      @JvmStatic
      fun fromValue(value: String): NotificationType? =
         NotificationType.values().firstOrNull { value.equals(it.value, ignoreCase = true) }
   }
}

data class Notification (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated
) : Entity<Notification> {

   constructor(dto: NotificationDto) :
      this(
         id = dto.id
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): Notification = copy()
}

@JsonInclude(NON_NULL)
data class NotificationDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null

) : DataTransferObjectBase<NotificationDto>() {

   constructor(entity: Notification) :
      this(
         id = entity.id
      )

   override fun dtoId(): Long? = id

   override fun copyMe(): NotificationDto = copy()
}
