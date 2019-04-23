package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.dto.NotificationRequestDto
import com.hightouchinc.cynergi.middleware.dto.NotificationResponseDto
import com.hightouchinc.cynergi.middleware.dto.NotificationsResponseDto
import com.hightouchinc.cynergi.middleware.entity.NotificationDto
import com.hightouchinc.cynergi.middleware.entity.NotificationTypeDomainDto
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.exception.ValidationException
import com.hightouchinc.cynergi.middleware.service.NotificationService
import com.hightouchinc.cynergi.middleware.validator.NotificationValidator
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.annotation.Status
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.micronaut.validation.Validated
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.validation.Valid

@Validated
@Secured(IS_AUTHENTICATED)
@Controller("/api/notifications") // TODO make company a first class part of this controller by defining it here
class NotificationController @Inject constructor(
   private val notificationService: NotificationService,
   private val notificationValidator: NotificationValidator
) {
   private val logger: Logger = LoggerFactory.getLogger(NotificationController::class.java)

   @Throws(NotFoundException::class)
   @Get("/{id}", produces = [APPLICATION_JSON])
   fun fetchOne(
      @QueryValue("id") id: Long
   ): NotificationResponseDto {
      logger.info("Fetching Notification by {}", id)

      val response = notificationService.fetchResponseById(id = id) ?: throw NotFoundException(id)

      logger.debug("Fetch Notification by {} resulted {}", id, response)

      return response
   }

   @Get(produces = [APPLICATION_JSON])
   @Deprecated(message = "Needs to be removed for a path based endpoint rather than header base", replaceWith = ReplaceWith("fetchAllByCompany and fetchAllByCompanyAndUser"))
   fun fetchAll(
      @Header("X-Auth-Company") companyId: String, // FIXME this needs to be made part of the path at some point
      @Header("X-Auth-User", defaultValue = EMPTY) authId: String,  // FIXME once cynergi-middleware is handling the authentication this should be pulled from the security mechanism
      @QueryValue(value = "type", defaultValue = "E") type: String
   ) : NotificationsResponseDto { // FIXME do away with this wrapper for the list of notifications, and make pageable
      logger.info("Fetching All Notifications by company: {}, authId: {}, type: {}", companyId, authId, type)

      val response = when(type.toUpperCase()) {
         "A" -> notificationService.fetchAllByCompanyWrapped(companyId = companyId, type = type)

         else -> notificationService.fetchAllByRecipientWrapped(companyId = companyId, sendingEmployee = authId, type = type)
      }

      logger.debug("Fetching All Notifications by company: {}, authId: {}, type: {} resulted in {}", companyId, authId, type, response)

      return response
   }

   @Get("/admin", produces = [APPLICATION_JSON])
   @Deprecated(message = "Needs to be removed for a path based endpoint rather than header base", replaceWith = ReplaceWith("fetchAllByCompany and fetchAllByCompanyAndUser"))
   fun fetchAllAdmin(
      @Header("X-Auth-Company") companyId: String, // FIXME this needs to be made part of the path at some point
      @Header("X-Auth-User") authId: String  // FIXME once cynergi-middleware is handling the authentication this should be pulled from the security mechanism
   ) : NotificationsResponseDto { // FIXME do away with this wrapper for the list of notifications
      logger.info("Fetching All Notifications by Admin by company: {}, authId: {}", companyId, authId)

      val response = notificationService.findAllBySendingEmployee(companyId = companyId, sendingEmployee = authId)

      logger.debug("Fetching All Notifications by Admin by company: {}, authId: {} resulted in {}", companyId, authId, response)

      return response
   }

   @Get("/permissions", produces = [APPLICATION_JSON])
   @Deprecated(message = "This is here for the original front-end for looking up permissions by department", replaceWith = ReplaceWith("something that handles this as yet TBD"))
   fun fetchPermissions(): Map<String, Any> {
      val response = mapOf(
         "id" to 1,
         "depts_allowed" to listOf("ALL")
      )

      logger.debug("Fetch Permissions resulted in {}", response)

      return response
   }

   @Get("/types", produces = [APPLICATION_JSON])
   fun fetchAllTypes(): List<NotificationTypeDomainDto> {
      logger.info("Fetching All Notification Type Domains")

      val response = notificationService.findAllTypes()

      logger.debug("Fetching All Notification Type Domains resulted in {}", response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get("/company/{companyId}", produces = [APPLICATION_JSON])
   fun fetchAllByCompany(
      @QueryValue("companyId") companyId: String,
      @QueryValue(value = "type", defaultValue = "E") type: String
   ): List<NotificationDto> {
      logger.info("Fetch all notifications by company {}, type {}", companyId, type)

      val response = notificationService.fetchAllByCompany(companyId = companyId, type = type)

      logger.debug("Fetch all notifications by company {}, type {} resulted in {}", companyId, type, response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get("/company/{companyId}/{sendingEmployee}", produces = [APPLICATION_JSON])
   fun fetchAllByCompanyAndUser(
      @QueryValue("companyId") companyId: String,
      @QueryValue("sendingEmployee") sendingEmployee: String,
      @QueryValue(value = "type", defaultValue = "E") type: String
   ): List<NotificationDto> {
      logger.info("Fetch All Notifications by Company and User with company: {}, sendingEmployee: {}, type: {}", companyId, sendingEmployee, type)

      val response = notificationService.fetchAllByRecipient(companyId = companyId, sendingEmployee = sendingEmployee, type = type)

      logger.debug("Fetch All Notifications by Company and User with company: {}, sendingEmployee: {}, type: {} resulted in {}", companyId, sendingEmployee, type, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun save(
      @Valid @Body dto: NotificationRequestDto
   ): NotificationResponseDto {
      logger.info("Requested Save Notification {}", dto)

      notificationValidator.validateSave(dto = dto.notification)

      val response = notificationService.create(dto = dto.notification)

      logger.debug("Requested Save Notification {} resulted in {}", dto, response)

      return NotificationResponseDto(notification = response)
   }

   @Put("/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   fun update(
      @QueryValue("id") id: Long,
      @Valid @Body dto: NotificationRequestDto
   ): NotificationResponseDto {
      val notificationDto = dto.notification.copy(id = id) // the legacy front-end doesn't pass in the id as part of the request body, it is part of the path instead
      logger.info("Requested Update Notification {}", notificationDto)

      notificationValidator.validateUpdate(dto = notificationDto)

      val response = notificationService.update(dto = notificationDto)

      logger.debug("Requested Update Notification {} resulted in {}", notificationDto, response)

      return NotificationResponseDto(notification = response)
   }

   @Delete("/{id}")
   @Status(HttpStatus.NO_CONTENT)
   fun delete(
      @QueryValue("id") id: Long
   ) {
      notificationService.delete(id = id)
   }
}
