package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.dto.NotificationResponseDto
import com.hightouchinc.cynergi.middleware.dto.NotificationsResponseDto
import com.hightouchinc.cynergi.middleware.entity.NotificationDto
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
   @Throws(NotFoundException::class)
   @Get(value = "/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("id") id: Long
   ): NotificationResponseDto {
      return notificationService.fetchResponseById(id = id) ?: throw NotFoundException(id)
   }

   @Throws(NotFoundException::class)
   @Get(produces = [APPLICATION_JSON])
   fun fetchAll(
      @Header("X-Auth-Company") companyId: String, // FIXME this needs to be made part of the path at some point
      @Header("X-Auth-User") authId: String,  // FIXME once cynergi-middleware is handling the authentication this should be pulled from the security mechanism
      @PathVariable(name = "type", defaultValue = "E") type: String
   ) : NotificationsResponseDto {
      return when(type.toUpperCase()) {
         "A" -> notificationService.fetchAllByCompany(companyId = companyId, type = type)

         else -> notificationService.fetchAllByRecipient(companyId = companyId, authId = authId, type = type)
      }
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
