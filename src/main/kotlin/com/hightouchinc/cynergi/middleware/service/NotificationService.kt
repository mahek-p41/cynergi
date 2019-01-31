package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.dto.NotificationResponseDto
import com.hightouchinc.cynergi.middleware.dto.NotificationsResponseDto
import com.hightouchinc.cynergi.middleware.entity.Notification
import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.middleware.repository.NotificationRepository
import com.hightouchinc.cynergi.middleware.repository.NotificationTypeDomainRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
   private val notificationRepository: NotificationRepository,
   private val notificationTypeDomainRepository: NotificationTypeDomainRepository
) : IdentifiableService<NotificationDto> {
   override fun fetchById(id: Long): NotificationDto? =
      notificationRepository.findOne(id = id)?.let { NotificationDto(entity = it) }

   /**
    * Acts a wrapper to map the original front-end expectation of an object with a notification property pointing to the
    * notification that was in the DB.
    *
    * FIXME by removing me someday
    */
   @Deprecated("Remove this when the front end just consumes the DTO without the wrapper", ReplaceWith(expression = "Should not be replaced just removed"))
   fun fetchResponseById(id: Long): NotificationResponseDto? =
      fetchById(id = id)?.let { NotificationResponseDto(notification = it) }

   fun fetchAllByCompany(companyId: String, type: String): NotificationsResponseDto =
      NotificationsResponseDto(
         notifications = notificationRepository.findAllByCompany(companyId = companyId, type = type).map { NotificationDto(it) }
      )

   fun fetchAllByRecipient(companyId: String, authId: String, type: String): NotificationsResponseDto =
      NotificationsResponseDto(
         notifications = notificationRepository.findAllByRecipient(companyId = companyId, recipientId = authId, type = type).map { NotificationDto(it) }
      )

   fun exists(id: Long): Boolean =
      notificationRepository.exists(id = id)

   fun create(dto: NotificationDto): NotificationDto {
      val notificationDomainType = notificationTypeDomainRepository.findOne(dto.notificationType!!)!!

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
      val notificationDomainType = notificationTypeDomainRepository.findOne(dto.notificationType!!)!!

      return NotificationDto(
         entity = notificationRepository.update(entity = Notification(dto = dto, notificationDomainType = notificationDomainType))
      )
   }
}
