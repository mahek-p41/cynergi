package com.cynergisuite.middleware.purchase.order.control.infrastructure

import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.purchase.order.control.PurchaseOrderControlDTO
import com.cynergisuite.middleware.purchase.order.control.PurchaseOrderControlService
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/purchase/order/control")
class PurchaseOrderControlController @Inject constructor(
   private val purchaseOrderControlService: PurchaseOrderControlService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderControlController::class.java)

   @Get(produces = [APPLICATION_JSON])
   @Operation(tags = ["PurchaseOrderControlEndpoints"], summary = "Fetch a single PurchaseOrderControlDTO", description = "Fetch a single PurchaseOrderControlDTO that is associated with the logged-in user's company", operationId = "purchaseOrderControl-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderControlDTO::class))]),
      ApiResponse(responseCode = "404", description = "The requested PurchaseOrderControl was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      authentication: Authentication
   ): PurchaseOrderControlDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Fetching PurchaseOrderControl by {}", userCompany)

      val response = purchaseOrderControlService.fetchOne(userCompany) ?: throw NotFoundException("Purchase order of the company")

      logger.debug("Fetching PurchaseOrderControl by {} resulted in", userCompany, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["PurchaseOrderControlEndpoint"], summary = "Create a PurchaseOrderControlEntity", description = "Create a PurchaseOrderControlEntity", operationId = "purchaseOrderControl-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderControlDTO::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   @AccessControl("POCTLUP")
   fun create(
      @Body dto: PurchaseOrderControlDTO,
      authentication: Authentication
   ): PurchaseOrderControlDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Create PurchaseOrderControl {}", dto)

      val response = purchaseOrderControlService.create(dto, userCompany)

      logger.debug("Requested Create PurchaseOrderControl {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["PurchaseOrderControlEndpoints"], summary = "Update a PurchaseOrderControlEntity", description = "Update a PurchaseOrderControlEntity from a body of PurchaseOrderControlDTO", operationId = "purchaseOrderControl-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to update PurchaseOrderControl", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderControlDTO::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "404", description = "The requested PurchaseOrderControl was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   @AccessControl("POCTLUP")
   fun update(
      @QueryValue("id") id: Long,
      @Body dto: PurchaseOrderControlDTO,
      authentication: Authentication
   ): PurchaseOrderControlDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update PurchaseOrderControl {}", dto)

      val response = purchaseOrderControlService.update(id, dto, userCompany)

      logger.debug("Requested Update PurchaseOrderControl {} resulted in {}", dto, response)

      return response
   }
}
