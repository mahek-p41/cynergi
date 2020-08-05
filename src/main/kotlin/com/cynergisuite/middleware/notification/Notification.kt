package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.Identifiable
import java.time.LocalDate
import java.time.OffsetDateTime

data class Notification(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val startDate: LocalDate,
   val expirationDate: LocalDate,
   val message: String,
   val sendingEmployee: String, // TODO convert from soft foreign key to employee
   val company: String, // TODO convert from soft foreign key to point to a company, does this even need to exist since you'd be able to walk the customer_account back up to get the company
   val notificationDomainType: NotificationType,
   val recipients: MutableSet<NotificationRecipient> = mutableSetOf()
) : Identifiable {

   constructor(startDate: LocalDate, expirationDate: LocalDate, message: String, sendingEmployee: String, company: String, notificationDomainType: NotificationType) :
      this (
         id = null,
         startDate = startDate,
         expirationDate = expirationDate,
         message = message,
         sendingEmployee = sendingEmployee,
         company = company,
         notificationDomainType = notificationDomainType
      )

   constructor(dto: NotificationValueObject, notificationDomainType: NotificationType) :
      this(
         id = dto.id,
         company = dto.company!!,
         expirationDate = dto.expirationDate!!,
         message = dto.message!!,
         sendingEmployee = dto.sendingEmployee!!,
         startDate = dto.startDate!!,
         notificationDomainType = notificationDomainType
      ) {

         dto.recipients.asSequence()
            .map { NotificationRecipient(it, this) }
            .forEach { this.recipients.add(it) }
      }

   override fun myId(): Long? = id
}
