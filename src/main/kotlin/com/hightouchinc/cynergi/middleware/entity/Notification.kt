package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.NOT_NULL
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class Notification (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val company: String, // TODO convert from soft foreign key to point to a company, does this even need to exist since you'd be able to walk the customer_account back up to get the company
   val expirationDate: LocalDate,
   val message: String,
   val sendingEmployee: String, // TODO convert from soft foreign key to employee
   val startDate: LocalDate,
   val notificationDomainType: NotificationTypeDomain
) : Entity<Notification> {

   constructor(dto: NotificationDto, notificationDomainType: NotificationTypeDomain) :
      this(
         id = dto.id,
         company = dto.company!!,
         expirationDate = dto.expirationDate!!,
         message = dto.message!!,
         sendingEmployee = dto.sendingEmployee!!,
         startDate = dto.startDate!!,
         notificationDomainType = notificationDomainType
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): Notification = copy()
}

@JsonInclude(NON_NULL)
data class NotificationDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:NotNull(message = NOT_NULL)
   val company: String?,

   @field:NotNull(message = NOT_NULL)
   val expirationDate: LocalDate?,

   @field:NotNull(message = NOT_NULL)
   val message: String?,

   @field:NotNull(message = NOT_NULL)
   val sendingEmployee: String?,

   @field:NotNull(message = NOT_NULL)
   val startDate: LocalDate?,

   @field:NotNull(message = NOT_NULL)
   val notificationType: String?

) : DataTransferObjectBase<NotificationDto>() {

   constructor(entity: Notification) :
      this(
         id = entity.id,
         company = entity.company,
         expirationDate = entity.expirationDate,
         message = entity.message,
         sendingEmployee = entity.sendingEmployee,
         startDate = entity.startDate,
         notificationType = "${entity.notificationDomainType.myValue()}:${entity.notificationDomainType.myDescription()}"
      )

   override fun dtoId(): Long? = id

   override fun copyMe(): NotificationDto = copy()
}
