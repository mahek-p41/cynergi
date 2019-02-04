package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.dto.NotificationResponseDto
import com.hightouchinc.cynergi.middleware.dto.NotificationsResponseDto
import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.middleware.entity.NotificationTypeDomainDto
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.NotificationService
import com.hightouchinc.cynergi.middleware.validator.NotificationValidator
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.validation.Validated
import org.apache.commons.lang3.StringUtils.EMPTY
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

   @Get(produces = [APPLICATION_JSON])
   fun fetchAll(
      @Header("X-Auth-Company") companyId: String, // FIXME this needs to be made part of the path at some point
      @Header("X-Auth-User", defaultValue = EMPTY) authId: String,  // FIXME once cynergi-middleware is handling the authentication this should be pulled from the security mechanism
      @QueryValue(value = "type", defaultValue = "E") type: String
   ) : NotificationsResponseDto { // FIXME do away with this wrapper for the list of notifications
      return when(type.toUpperCase()) {
         "A" -> notificationService.fetchAllByCompanyWrapped(companyId = companyId, type = type)

         else -> notificationService.fetchAllByRecipientWrapped(companyId = companyId, authId = authId, type = type)
      }
   }

   @Get(produces = [APPLICATION_JSON])
   fun fetchAllAdmin(
      @Header("X-Auth-Company") companyId: String, // FIXME this needs to be made part of the path at some point
      @Header("X-Auth-User") authId: String  // FIXME once cynergi-middleware is handling the authentication this should be pulled from the security mechanism
   ) : NotificationsResponseDto { // FIXME do away with this wrapper for the list of notifications
      return notificationService.findAllBySendingEmployee(companyId = companyId, sendingEmployee = authId)
   }

   @Get(value = "/types", produces = [APPLICATION_JSON])
   fun fetchAllTypes(): List<NotificationTypeDomainDto> =
      notificationService.findAllTypes()

   @Throws(NotFoundException::class)
   @Get("/company/{companyId}", produces = [APPLICATION_JSON])
   fun fetchAllByCompany(
      @QueryValue("companyId") companyId: String,
      @QueryValue(value = "type", defaultValue = "E") type: String
   ): List<NotificationDto> {
      return notificationService.fetchAllByCompany(companyId = companyId, type = type)
   }

   @Throws(NotFoundException::class)
   @Get("/company/{companyId}/{authId}", produces = [APPLICATION_JSON])
   fun fetchAllByCompanyAndUser(
      @QueryValue("companyId") companyId: String,
      @QueryValue("authId") authId: String,
      @QueryValue(value = "type", defaultValue = "E") type: String
   ): List<NotificationDto> {
      return notificationService.fetchAllByRecipient(companyId = companyId, authId = authId, type = type)
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
