package com.hightouchinc.cynergi.middleware.service

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

   fun fetchAllByCompany(companyId: String, type: String): List<NotificationDto> {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }

   fun fetchAllByRecipient(companyId: String, authId: String, type: String): List<NotificationDto> {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }
}
