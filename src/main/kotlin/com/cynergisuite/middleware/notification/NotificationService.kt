package com.cynergisuite.middleware.notification

import com.cynergisuite.domain.infrastructure.IdentifiableService
import com.cynergisuite.middleware.notification.infrastructure.NotificationRepository
import com.cynergisuite.middleware.notification.infrastructure.NotificationTypeDomainRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
   private val notificationRepository: NotificationRepository,
   private val notificationTypeDomainRepository: NotificationTypeDomainRepository
) : IdentifiableService<NotificationValueObject> {
   override fun fetchById(id: Long): NotificationValueObject? =
      notificationRepository.findOne(id = id)?.let { NotificationValueObject(entity = it) }

   fun fetchAllByCompany(companyId: String, type: String): List<NotificationValueObject> =
      notificationRepository.findAllByCompany(companyId = companyId, type = type).map { NotificationValueObject(it) }

   fun fetchAllByRecipient(companyId: String, sendingEmployee: String, type: String): List<NotificationValueObject> =
      notificationRepository.findAllByRecipient(companyId = companyId, recipientId = sendingEmployee, type = type).map { NotificationValueObject(it) }

   fun findAllTypes(): List<NotificationTypeValueObject> =
      notificationRepository.findAllTypes().map { NotificationTypeValueObject(it) }

   /**
    * Acts as a wrapper to map the original front-end expectation of an object with a notifications property pointing to the
    * notification that was in the DB.
    *
    * FIXME by removing me someday
    */
   @Deprecated("Remove this when the front end just consumes the DTO without the wrapper", ReplaceWith(expression = "Should not be replaced just removed"))
   fun fetchResponseById(id: Long): NotificationResponseValueObject? =
      fetchById(id = id)?.let { NotificationResponseValueObject(notification = it) }

   /**
    * Acts as a wrapper to map the original front-end expectation of an object with a notifications property pointing to the
    * notification that was in the DB.
    *
    * FIXME by removing me someday
    */
   @Deprecated("Remove this when the front end just consumes the DTO without the wrapper", ReplaceWith(expression = "Should not be replaced just removed"))
   fun fetchAllByCompanyWrapped(companyId: String, type: String): NotificationsResponseValueObject =
      NotificationsResponseValueObject(
         notifications = fetchAllByCompany(companyId = companyId, type = type)
      )

   /**
    * Acts as a wrapper to map the original front-end expectation of an object with a notifications property pointing to the
    * notification that was in the DB.
    *
    * FIXME by removing me someday
    */
   @Deprecated("Remove this when the front end just consumes the DTO without the wrapper", ReplaceWith(expression = "Should not be replaced just removed"))
   fun findAllBySendingEmployee(companyId: String, sendingEmployee: String): NotificationsResponseValueObject =
      NotificationsResponseValueObject(
         notifications = notificationRepository.findAllBySendingEmployee(companyId = companyId, sendingEmployee = sendingEmployee).map { NotificationValueObject(it) }
      )


   /**
    * Acts as a wrapper to map the original front-end expectation of an object with a notifications property pointing to the
    * notification that was in the DB.
    *
    * FIXME by removing me someday
    */
   @Deprecated("Remove this when the front end just consumes the DTO without the wrapper", ReplaceWith(expression = "Should not be replaced just removed"))
   fun fetchAllByRecipientWrapped(companyId: String, sendingEmployee: String, type: String): NotificationsResponseValueObject =
      NotificationsResponseValueObject(
         notifications = fetchAllByRecipient(companyId = companyId, sendingEmployee = sendingEmployee, type = type)
      )

   override fun exists(id: Long): Boolean =
      notificationRepository.exists(id = id)

   fun create(dto: NotificationValueObject): NotificationValueObject {
      val notificationDomainType = notificationTypeDomainRepository.findOne(dto.notificationType!!.substringBefore(":"))!!

      return NotificationValueObject(
         entity = notificationRepository.insert(
            entity = Notification(
               dto = dto,
               notificationDomainType = notificationDomainType
            )
         )
      )
   }

   fun update(dto: NotificationValueObject): NotificationValueObject {
      val notificationDomainType = notificationTypeDomainRepository.findOne(dto.notificationType!!.substringBefore(":"))!!

      return NotificationValueObject(
         entity = notificationRepository.update(entity = Notification(dto = dto, notificationDomainType = notificationDomainType))
      )
   }

   fun delete(id: Long): Int =
      notificationRepository.delete(id = id)
}
