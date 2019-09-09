package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "Notification", title = "Single Notification", description = "Describes a single notification")
data class NotificationValueObject (

   @field:Positive
   var id: Long? = null,

   @JsonFormat(pattern = "yyyy-MM-dd")
   var dateCreated: LocalDate?,

   @field:NotNull
   var startDate: LocalDate?,

   @field:NotNull
   var expirationDate: LocalDate?,

   @field:JsonProperty("companyId") // FIXME remove this when the front-end for this is rewritten
   @field:NotNull
   @field:Size(min = 6, max = 6)
   var company: String?,

   @field:NotNull
   @field:Size(max = 500)
   var message: String?,

   @field:NotNull
   @field:Size(max = 255)
   var sendingEmployee: String?,

   @field:NotNull
   @field:Size(min = 1)
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

   override fun valueObjectId(): Long? = id

   override fun copyMe(): NotificationValueObject = copy()
}
