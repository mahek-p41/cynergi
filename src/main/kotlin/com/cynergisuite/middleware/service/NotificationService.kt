package com.cynergisuite.middleware.service

import com.cynergisuite.middleware.dto.NotificationResponseDto
import com.cynergisuite.middleware.dto.NotificationsResponseDto
import com.cynergisuite.middleware.notification.Notification
import com.cynergisuite.middleware.notification.NotificationDto
import com.cynergisuite.middleware.notification.NotificationTypeDomainDto
import com.cynergisuite.middleware.notification.infrastructure.NotificationRepository
import com.cynergisuite.middleware.notification.infrastructure.NotificationTypeDomainRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
   private val notificationRepository: NotificationRepository,
   private val notificationTypeDomainRepository: NotificationTypeDomainRepository
) : IdentifiableService<NotificationDto> {
   override fun fetchById(id: Long): NotificationDto? =
      notificationRepository.findOne(id = id)?.let { NotificationDto(entity = it) }

   fun fetchAllByCompany(companyId: String, type: String): List<NotificationDto> =
      notificationRepository.findAllByCompany(companyId = companyId, type = type).map { NotificationDto(it) }

   fun fetchAllByRecipient(companyId: String, sendingEmployee: String, type: String): List<NotificationDto> =
      notificationRepository.findAllByRecipient(companyId = companyId, recipientId = sendingEmployee, type = type).map { NotificationDto(it) }

   fun findAllTypes(): List<NotificationTypeDomainDto> =
      notificationRepository.findAllTypes().map { NotificationTypeDomainDto(it) }

   /**
    * Acts as a wrapper to map the original front-end expectation of an object with a notifications property pointing to the
    * notification that was in the DB.
    *
    * FIXME by removing me someday
    */
   @Deprecated("Remove this when the front end just consumes the DTO without the wrapper", ReplaceWith(expression = "Should not be replaced just removed"))
   fun fetchResponseById(id: Long): NotificationResponseDto? =
      fetchById(id = id)?.let { NotificationResponseDto(notification = it) }

   /**
    * Acts as a wrapper to map the original front-end expectation of an object with a notifications property pointing to the
    * notification that was in the DB.
    *
    * FIXME by removing me someday
    */
   @Deprecated("Remove this when the front end just consumes the DTO without the wrapper", ReplaceWith(expression = "Should not be replaced just removed"))
   fun fetchAllByCompanyWrapped(companyId: String, type: String): NotificationsResponseDto =
      NotificationsResponseDto(
         notifications = fetchAllByCompany(companyId = companyId, type = type)
      )

   /**
    * Acts as a wrapper to map the original front-end expectation of an object with a notifications property pointing to the
    * notification that was in the DB.
    *
    * FIXME by removing me someday
    */
   @Deprecated("Remove this when the front end just consumes the DTO without the wrapper", ReplaceWith(expression = "Should not be replaced just removed"))
   fun findAllBySendingEmployee(companyId: String, sendingEmployee: String): NotificationsResponseDto =
      NotificationsResponseDto(
         notifications = notificationRepository.findAllBySendingEmployee(companyId = companyId, sendingEmployee = sendingEmployee).map { NotificationDto(it) }
      )


   /**
    * Acts as a wrapper to map the original front-end expectation of an object with a notifications property pointing to the
    * notification that was in the DB.
    *
    * FIXME by removing me someday
    */
   @Deprecated("Remove this when the front end just consumes the DTO without the wrapper", ReplaceWith(expression = "Should not be replaced just removed"))
   fun fetchAllByRecipientWrapped(companyId: String, sendingEmployee: String, type: String): NotificationsResponseDto =
      NotificationsResponseDto(
         notifications = fetchAllByRecipient(companyId = companyId, sendingEmployee = sendingEmployee, type = type)
      )

   override fun exists(id: Long): Boolean =
      notificationRepository.exists(id = id)

   fun create(dto: NotificationDto): NotificationDto {
      val notificationDomainType = notificationTypeDomainRepository.findOne(dto.notificationType!!.substringBefore(":"))!!

      return NotificationDto(
         entity = notificationRepository.insert(
            entity = Notification(
               dto = dto,
               notificationDomainType = notificationDomainType
            )
         )
      )
   }

   fun update(dto: NotificationDto): NotificationDto {
      val notificationDomainType = notificationTypeDomainRepository.findOne(dto.notificationType!!.substringBefore(":"))!!

      return NotificationDto(
         entity = notificationRepository.update(entity = Notification(dto = dto, notificationDomainType = notificationDomainType))
      )
   }

   fun delete(id: Long): Int =
      notificationRepository.delete(id = id)
}
