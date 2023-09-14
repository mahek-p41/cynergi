package com.cynergisuite.middleware.notification.infrastructure

import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.notification.NotificationRequestValueObject
import com.cynergisuite.middleware.notification.NotificationResponseValueObject
import com.cynergisuite.middleware.notification.NotificationService
import com.cynergisuite.middleware.notification.NotificationTypeValueObject
import com.cynergisuite.middleware.notification.NotificationValidator
import com.cynergisuite.middleware.notification.NotificationValueObject
import com.cynergisuite.middleware.notification.NotificationsResponseValueObject
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
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.apache.commons.lang3.StringUtils.EMPTY
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

// TODO make company a first class part of this controller by defining it here, and remove the plural form
@Secured(IS_ANONYMOUS)
@Controller("/api/notifications")
class NotificationController @Inject constructor(
   private val notificationService: NotificationService,
   private val notificationValidator: NotificationValidator
) {
   private val logger: Logger = LoggerFactory.getLogger(NotificationController::class.java)

   @Throws(NotFoundException::class)
   @Get("/{id}", produces = [APPLICATION_JSON])
   @Operation(tags = ["NotificationEndpoints"], summary = "Fetch a single Notification", description = "Fetch a single Notification by it's system generated primary key", operationId = "notification-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "The Notification was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = NotificationResponseValueObject::class))]),
         ApiResponse(responseCode = "404", description = "The requested Notification was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Parameter(name = "id", description = "The Notification ID to lookup", required = true, `in` = PATH) @QueryValue("id")
      id: Long
   ): NotificationResponseValueObject {
      logger.trace("Fetching Notification by {}", id)

      val response = notificationService.fetchResponseById(id = id) ?: throw NotFoundException(id)

      logger.trace("Fetch Notification by {} resulted {}", id, response)

      return response
   }

   @Get(produces = [APPLICATION_JSON])
   @Deprecated(message = "Needs to be removed for a path based endpoint rather than header base", replaceWith = ReplaceWith("fetchAllByCompany and fetchAllByCompanyAndUser"))
   @Operation(tags = ["NotificationEndpoints"], summary = "Fetch a listing of Notifications", description = "Fetch a listing of Notifications by it's system generated primary key", operationId = "notification-fetchAll", deprecated = true)
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "Listing of notifications was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = NotificationsResponseValueObject::class))]),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "X-Auth-Company", `in` = HEADER, required = true, schema = Schema(type = "string")) @Header("X-Auth-Company")
      companyId: String, // FIXME this needs to be made part of the path at some point
      @Parameter(name = "X-Auth-User", `in` = HEADER) @Header("X-Auth-User", defaultValue = EMPTY)
      authId: String, // FIXME once the front-end is using the framework's JWT
      @Parameter(name = "type", description = "The type of notifications to be loaded", required = false, `in` = QUERY, schema = Schema(type = "string", defaultValue = "E")) @QueryValue(value = "type", defaultValue = "E")
      type: String
   ): NotificationsResponseValueObject { // FIXME do away with this wrapper for the list of notifications, and make pageable
      logger.trace("Fetching All Notifications by company: {}, authId: {}, type: {}", companyId, authId, type)

      val response = when (type.uppercase()) {
         "A" -> notificationService.fetchAllByCompanyWrapped(companyId = companyId, type = type)

         else -> notificationService.fetchAllByRecipientWrapped(
            companyId = companyId,
            sendingEmployee = authId,
            type = type
         )
      }

      logger.debug(
         "Fetching All Notifications by company: {}, authId: {}, type: {} resulted in {}",
         companyId,
         authId,
         type,
         response
      )

      return response
   }

   @Get("/admin", produces = [APPLICATION_JSON])
   @Deprecated(message = "Needs to be removed for a path based endpoint rather than header base", replaceWith = ReplaceWith("fetchAllByCompany and fetchAllByCompanyAndUser"))
   @Operation(tags = ["NotificationEndpoints"], summary = "Fetch a listing of Notifications as an Admin", description = "Fetch a listing of Notifications by it's Company and User", operationId = "notificationAdmin-fetchAll", deprecated = true)
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "Listing of notifications was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = NotificationsResponseValueObject::class))]),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllAdmin(
      @Parameter(name = "X-Auth-Company", `in` = HEADER, required = true, schema = Schema(type = "string")) @Header("X-Auth-Company")
      companyId: String, // FIXME this needs to be made part of the path at some point
      @Parameter(name = "X-Auth-User", `in` = HEADER, schema = Schema(type = "string")) @Header("X-Auth-User")
      authId: String // FIXME once cynergi-middleware is handling the authentication this should be pulled from the security mechanism
   ): NotificationsResponseValueObject { // FIXME do away with this wrapper for the list of notifications
      logger.trace("Fetching All Notifications by Admin by company: {}, authId: {}", companyId, authId)

      val response = notificationService.findAllBySendingEmployee(companyId = companyId, sendingEmployee = authId)

      logger.trace("Fetching All Notifications by Admin by company: {}, authId: {} resulted in {}", companyId, authId, response)

      return response
   }

   @Get("/permissions", produces = [APPLICATION_JSON])
   @Deprecated(message = "This is here for the original front-end for looking up permissions by department", replaceWith = ReplaceWith("something that handles this as yet TBD"))
   @Operation(tags = ["NotificationEndpoints"], summary = "Fetch a listing of Notification Permissions", description = "Fetch a listing of Notification Permissions", operationId = "notificationPermissions-fetchAll", deprecated = true)
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "Listing of permissions was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Map::class))]),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchPermissions(): Map<String, Any> {
      val response = mapOf(
         "id" to 1,
         "depts_allowed" to listOf("ALL")
      )

      logger.trace("Fetch Permissions resulted in {}", response)

      return response
   }

   @Get("/types", produces = [APPLICATION_JSON])
   @Operation(tags = ["NotificationEndpoints"], summary = "Fetch a listing of Notification Types", description = "Fetch a listing of valid Notification Types defined by the system", operationId = "notificationTypes-fetchAllTypes")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "Listing of notifications was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Array<NotificationTypeValueObject>::class))]),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllTypes(): List<NotificationTypeValueObject> {
      logger.trace("Fetching All Notification Type Domains")

      val response = notificationService.findAllTypes()

      logger.trace("Fetching All Notification Type Domains resulted in {}", response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get("/company/{companyId}", produces = [APPLICATION_JSON])
   @Operation(tags = ["NotificationEndpoints"], summary = "Fetch a listing of Notification Types", description = "Fetch a listing of valid Notification Types defined by the system", operationId = "notifications-fetchAll-company")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "Listing of notifications was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Array<NotificationValueObject>::class))]),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllByCompany(
      @QueryValue("companyId") companyId: String,
      @QueryValue(value = "type", defaultValue = "E") type: String
   ): List<NotificationValueObject> {
      logger.trace("Fetch all notifications by company {}, type {}", companyId, type)

      val response = notificationService.fetchAllByCompany(companyId = companyId, type = type)

      logger.trace("Fetch all notifications by company {}, type {} resulted in {}", companyId, type, response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get("/company/{companyId}/{sendingEmployee}", produces = [APPLICATION_JSON])
   @Operation(tags = ["NotificationEndpoints"], summary = "Fetch a listing of Notifications by company and sending employee", operationId = "notifications-fetchAll-company-employee")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "Listing of notifications was able to be loaded", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Array<NotificationValueObject>::class))]),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllByCompanyAndUser(
      @QueryValue("companyId") companyId: String,
      @QueryValue("sendingEmployee") sendingEmployee: String,
      @QueryValue(value = "type", defaultValue = "E") type: String
   ): List<NotificationValueObject> {
      logger.trace("Fetch All Notifications by Company and User with company: {}, sendingEmployee: {}, type: {}", companyId, sendingEmployee, type)

      val response = notificationService.fetchAllByRecipient(companyId = companyId, sendingEmployee = sendingEmployee, type = type)

      logger.trace("Fetch All Notifications by Company and User with company: {}, sendingEmployee: {}, type: {} resulted in {}", companyId, sendingEmployee, type, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["NotificationEndpoints"], summary = "Create a single Notification", description = "Create a single Notification and return the resulting item", operationId = "notification-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the Notification update was successful", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = NotificationRequestValueObject::class))]),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Valid @Body dto: NotificationRequestValueObject
   ): NotificationResponseValueObject {
      logger.trace("Requested Create Notification {}", dto)

      notificationValidator.validateCreate(vo = dto.notification)

      val response = notificationService.create(dto = dto.notification)

      logger.trace("Requested Create Notification {} resulted in {}", dto, response)

      return NotificationResponseValueObject(notification = response)
   }

   @Put("/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["NotificationEndpoints"], summary = "Update a single Notification", description = "Update a single Notification by it's system generated primary key", operationId = "notification-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the Notification update was successful", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = NotificationRequestValueObject::class))]),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: Long,
      @Valid @Body
      dto: NotificationRequestValueObject
   ): NotificationResponseValueObject {
      val notificationValueObject = dto.notification.copy(id = id) // the legacy front-end doesn't pass in the id as part of the request body, it is part of the path instead
      logger.trace("Requested Update Notification {}", notificationValueObject)

      notificationValidator.validateUpdate(vo = notificationValueObject)

      val response = notificationService.update(dto = notificationValueObject)

      logger.trace("Requested Update Notification {} resulted in {}", notificationValueObject, response)

      return NotificationResponseValueObject(notification = response)
   }

   @Delete("/{id}")
   @Status(HttpStatus.NO_CONTENT)
   @Operation(tags = ["NotificationEndpoints"], summary = "Delete a single Notification", description = "Delete a single Notification by it's system generated primary key", operationId = "notification-deleteOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the operation produced no errors.  No error is thrown if the item didn't exist."),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: Long
   ) {
      notificationService.delete(id = id)
   }
}
