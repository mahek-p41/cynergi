package com.hightouchinc.cynergi.middleware.service

import com.hightouchinc.cynergi.middleware.dto.NotificationResponseDto
import com.hightouchinc.cynergi.middleware.dto.NotificationsResponseDto
import com.hightouchinc.cynergi.middleware.entity.Notification
import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.middleware.repository.NotificationsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
   private val notificationsRepository: NotificationsRepository
) : IdentifiableService<NotificationDto> {
   override fun fetchById(id: Long): NotificationDto? =
      notificationsRepository.findOne(id = id)?.let { NotificationDto(entity = it) }

   /**
    * Acts a wrapper to map the original front-end expectation of an object with a notification property pointing to the
    * notification that was in the DB.
    *
    * FIXME by removing me someday
    */
   @Deprecated("Remove this when the front end just consumes the DTO without the wrapper")
   fun fetchResponseById(id: Long): NotificationResponseDto? =
      fetchById(id = id)?.let { NotificationResponseDto(notification = it) }

   fun fetchAllByCompany(companyId: String, type: String): NotificationsResponseDto {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   fun fetchAllByRecipient(companyId: String, authId: String, type: String): NotificationsResponseDto {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   fun exists(id: Long): Boolean =
      notificationsRepository.exists(id = id)

   fun create(dto: NotificationDto): NotificationDto =
      NotificationDto(
         entity = notificationsRepository.insert(entity = Notification(dto = dto))
      )

   fun update(dto: NotificationDto): NotificationDto =
      NotificationDto(
         entity = notificationsRepository.update(entity = Notification(dto = dto))
      )
}
