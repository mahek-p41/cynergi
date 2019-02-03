package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.dto.NotificationResponseDto
import com.hightouchinc.cynergi.middleware.dto.NotificationsResponseDto
import com.hightouchinc.cynergi.middleware.entity.Notification
import com.hightouchinc.cynergi.middleware.entity.NotificationTypeDomainDto
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

   fun fetchAllByCompany(companyId: String, type: String): List<NotificationDto> =
      notificationRepository.findAllByCompany(companyId = companyId, type = type).map { NotificationDto(it) }

   fun fetchAllByRecipient(companyId: String, authId: String, type: String): List<NotificationDto> =
      notificationRepository.findAllByRecipient(companyId = companyId, recipientId = authId, type = type).map { NotificationDto(it) }

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
   fun fetchAllByRecipientWrapped(companyId: String, authId: String, type: String): NotificationsResponseDto =
      NotificationsResponseDto(
         notifications = fetchAllByRecipient(companyId = companyId, authId = authId, type = type)
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
