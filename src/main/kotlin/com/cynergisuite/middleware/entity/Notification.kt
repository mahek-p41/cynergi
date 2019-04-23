package com.cynergisuite.middleware.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.cynergisuite.middleware.dto.spi.DataTransferObjectBase
import com.cynergisuite.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import com.cynergisuite.middleware.localization.MessageCodes.Validation.SIZE
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class Notification (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val startDate: LocalDate,
   val expirationDate: LocalDate,
   val message: String,
   val sendingEmployee: String, // TODO convert from soft foreign key to employee
   val company: String, // TODO convert from soft foreign key to point to a company, does this even need to exist since you'd be able to walk the customer_account back up to get the company
   val notificationDomainType: com.cynergisuite.middleware.entity.NotificationTypeDomain,
   val recipients: MutableSet<com.cynergisuite.middleware.entity.NotificationRecipient> = mutableSetOf()
) : com.cynergisuite.middleware.entity.Entity<com.cynergisuite.middleware.entity.Notification> {

   constructor(startDate: LocalDate, expirationDate: LocalDate, message: String, sendingEmployee: String, company: String, notificationDomainType: com.cynergisuite.middleware.entity.NotificationTypeDomain) :
      this (
         id = null,
         startDate = startDate,
         expirationDate = expirationDate,
         message = message,
         sendingEmployee = sendingEmployee,
         company = company,
         notificationDomainType = notificationDomainType
      )

   constructor(dto: com.cynergisuite.middleware.entity.NotificationDto, notificationDomainType: com.cynergisuite.middleware.entity.NotificationTypeDomain) :
      this(
         id = dto.id,
         company = dto.company!!,
         expirationDate = dto.expirationDate!!,
         message = dto.message!!,
         sendingEmployee = dto.sendingEmployee!!,
         startDate = dto.startDate!!,
         notificationDomainType = notificationDomainType
      ) {

      dto.recipients.asSequence().map { com.cynergisuite.middleware.entity.NotificationRecipient(it, this) }.forEach { this.recipients.add(it) }
   }

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): com.cynergisuite.middleware.entity.Notification = copy()
}

@JsonInclude(NON_NULL)
data class NotificationDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
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

   var recipients: List<com.cynergisuite.middleware.entity.NotificationRecipientDto> = emptyList()

) : DataTransferObjectBase<com.cynergisuite.middleware.entity.NotificationDto>() {

   constructor(entity: com.cynergisuite.middleware.entity.Notification) :
      this(
         id = entity.id,
         dateCreated = entity.timeCreated.toLocalDate(),
         company = entity.company,
         expirationDate = entity.expirationDate,
         message = entity.message,
         sendingEmployee = entity.sendingEmployee,
         startDate = entity.startDate,
         notificationType = "${entity.notificationDomainType.value}:${entity.notificationDomainType.description}",
         recipients = entity.recipients.map { com.cynergisuite.middleware.entity.NotificationRecipientDto(it) }
      )

   constructor(id: Long?, message: String, entity: com.cynergisuite.middleware.entity.Notification) :
      this(
         id = id,
         dateCreated = entity.timeCreated.toLocalDate(),
         company = entity.company,
         expirationDate = entity.expirationDate,
         message = message,
         sendingEmployee = entity.sendingEmployee,
         startDate = entity.startDate,
         notificationType = "${entity.notificationDomainType.value}:${entity.notificationDomainType.description}",
         recipients = entity.recipients.map { com.cynergisuite.middleware.entity.NotificationRecipientDto(it) }
      )

   constructor(entity: com.cynergisuite.middleware.entity.Notification, recipients: List<com.cynergisuite.middleware.entity.NotificationRecipient>):
      this(
         entity = entity
      ) {

      this.recipients = recipients.map { com.cynergisuite.middleware.entity.NotificationRecipientDto(it) }
   }

   constructor(entity: com.cynergisuite.middleware.entity.Notification, notificationType: String) :
      this(
         id = entity.id,
         dateCreated = entity.timeCreated.toLocalDate(),
         company = entity.company,
         expirationDate = entity.expirationDate,
         message = entity.message,
         sendingEmployee = entity.sendingEmployee,
         startDate = entity.startDate,
         notificationType = notificationType,
         recipients = entity.recipients.map { com.cynergisuite.middleware.entity.NotificationRecipientDto(it) }
      )

   override fun dtoId(): Long? = id

   override fun copyMe(): com.cynergisuite.middleware.entity.NotificationDto = copy()
}
