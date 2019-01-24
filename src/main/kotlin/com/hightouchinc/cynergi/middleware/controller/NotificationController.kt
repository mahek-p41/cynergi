package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.middleware.entity.NotificationType
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.NotificationService
import com.hightouchinc.cynergi.middleware.validator.NotificationValidator
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.validation.Valid

@Validated
@Controller("/api/notifications")
class NotificationController @Inject constructor(
   private val notificationService: NotificationService,
   private val notificationValidator: NotificationValidator
) {
   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("id") id: Long
   ): NotificationDto {
      return notificationService.fetchById(id = id) ?: throw NotFoundException(id)
   }

   fun fetchAll(
      @Header("X-Auth-Company") companyId: String,
      @Header("X-Auth-User") authId: String,
      @PathVariable(name = "type", defaultValue = "E") type: String
   ) : Map<String, List<NotificationDto>> {
      val notificationType = NotificationType.fromValue(value = type) ?: throw NotFoundException(notFound = type)

      val notifications: List<NotificationDto> = when(notificationType) {
         NotificationType.All -> notificationService.fetchAllByCompany(companyId = companyId, type = type)
         else -> notificationService.fetchAllByRecipient(companyId = companyId, authId = authId, type = type)
      }

      return mapOf("notifications" to notifications)
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun save(
      @Valid @Body dto: NotificationDto
   ): NotificationDto {
      notificationValidator.validateSave(dto = dto)

      return notificationService.create(dto = dto)
   }

   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun update(
      @Valid @Body dto: NotificationDto
   ): NotificationDto {
      notificationValidator.validateUpdate(dto = dto)

      return notificationService.update(dto = dto)
   }
}
