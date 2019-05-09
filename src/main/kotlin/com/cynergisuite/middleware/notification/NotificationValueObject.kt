package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import com.cynergisuite.middleware.localization.MessageCodes.Validation.POSITIVE
import com.cynergisuite.middleware.localization.MessageCodes.Validation.SIZE
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class NotificationValueObject (

   @field:Positive(message = POSITIVE)
   var id: Long? = null,

   @JsonFormat(pattern = "yyyy-MM-dd")
   var dateCreated: LocalDate?,

   @field:NotNull(message = NOT_NULL)
   var startDate: LocalDate?,

   @field:NotNull(message = NOT_NULL)
   var expirationDate: LocalDate?,

   @field:JsonProperty("companyId") // FIXME remove this when the front-end for this is rewritten
   @field:NotNull(message = NOT_NULL)
   @field:Size(min = 6, max = 6, message = SIZE)
   var company: String?,

   @field:NotNull(message = NOT_NULL)
   @field:Size(max = 500, message = SIZE)
   var message: String?,

   @field:NotNull(message = NOT_NULL)
   @field:Size(max = 255, message = SIZE)
   var sendingEmployee: String?,

   @field:NotNull(message = NOT_NULL)
   @field:Size(min = 1, message = SIZE)
   var notificationType: String?,

   var recipients: List<NotificationRecipientValueObject> = emptyList()

) : ValueObjectBase<NotificationValueObject>() {

   constructor(entity: Notification) :
      this(
         id = entity.id,
         dateCreated = entity.timeCreated.toLocalDate(),
         company = entity.company,
         expirationDate = entity.expirationDate,
         message = entity.message,
         sendingEmployee = entity.sendingEmployee,
         startDate = entity.startDate,
         notificationType = "${entity.notificationDomainType.value}:${entity.notificationDomainType.description}",
         recipients = entity.recipients.map { NotificationRecipientValueObject(it) }
      )

   constructor(id: Long?, message: String, entity: Notification) :
      this(
         id = id,
         dateCreated = entity.timeCreated.toLocalDate(),
         company = entity.company,
         expirationDate = entity.expirationDate,
         message = message,
         sendingEmployee = entity.sendingEmployee,
         startDate = entity.startDate,
         notificationType = "${entity.notificationDomainType.value}:${entity.notificationDomainType.description}",
         recipients = entity.recipients.map { NotificationRecipientValueObject(it) }
      )

   constructor(entity: Notification, recipients: List<NotificationRecipient>):
      this(
         entity = entity
      ) {

      this.recipients = recipients.map { NotificationRecipientValueObject(it) }
   }

   constructor(entity: Notification, notificationType: String) :
      this(
         id = entity.id,
         dateCreated = entity.timeCreated.toLocalDate(),
         company = entity.company,
         expirationDate = entity.expirationDate,
         message = entity.message,
         sendingEmployee = entity.sendingEmployee,
         startDate = entity.startDate,
         notificationType = notificationType,
         recipients = entity.recipients.map { NotificationRecipientValueObject(it) }
      )

   override fun dtoId(): Long? = id

   override fun copyMe(): NotificationValueObject = copy()
}
