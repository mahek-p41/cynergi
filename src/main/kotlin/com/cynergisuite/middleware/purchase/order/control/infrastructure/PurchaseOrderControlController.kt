package com.cynergisuite.middleware.purchase.order.control.infrastructure

import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.purchase.order.control.PurchaseOrderControlDTO
import com.cynergisuite.middleware.purchase.order.control.PurchaseOrderControlService
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/purchase-order/control")
class PurchaseOrderControlController @Inject constructor(
   private val purchaseOrderControlService: PurchaseOrderControlService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderControlController::class.java)

   @Get(produces = [APPLICATION_JSON])
   @Operation(tags = ["PurchaseOrderControlEndpoints"], summary = "Fetch a single PurchaseOrderControlDTO", description = "Fetch a single PurchaseOrderControlDTO that is associated with the logged-in user's company", operationId = "purchaseOrderControl-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderControlDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested PurchaseOrderControl was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      authentication: Authentication
   ): PurchaseOrderControlDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Fetching PurchaseOrderControl by {}", userCompany)

      val response = purchaseOrderControlService.fetchOne(userCompany) ?: throw NotFoundException("Purchase order of the company")

      logger.debug("Fetching PurchaseOrderControl by {} resulted in", userCompany, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["PurchaseOrderControlEndpoint"], summary = "Create a PurchaseOrderControlEntity", description = "Create a PurchaseOrderControlEntity", operationId = "purchaseOrderControl-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderControlDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: PurchaseOrderControlDTO,
      authentication: Authentication
   ): PurchaseOrderControlDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Create PurchaseOrderControl {}", dto)

      val response = purchaseOrderControlService.create(dto, userCompany)

      logger.debug("Requested Create PurchaseOrderControl {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["PurchaseOrderControlEndpoints"], summary = "Update a PurchaseOrderControlEntity", description = "Update a PurchaseOrderControlEntity from a body of PurchaseOrderControlDTO", operationId = "purchaseOrderControl-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update PurchaseOrderControl", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderControlDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested PurchaseOrderControl was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: UUID,
      @Body @Valid
      dto: PurchaseOrderControlDTO,
      authentication: Authentication
   ): PurchaseOrderControlDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update PurchaseOrderControl {}", dto)

      val response = purchaseOrderControlService.update(id, dto, userCompany)

      logger.debug("Requested Update PurchaseOrderControl {} resulted in {}", dto, response)

      return response
   }

   @Get(uri = "/approver", produces = [APPLICATION_JSON])
   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["PurchaseOrderControlEndpoints"], summary = "Fetch a listing of approvers", description = "Fetch a listing of employee who have sufficient security to Change a Purchase Order", operationId = "purchaseOrderControl-fetchApprover")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = EmployeeValueObject::class))]),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchApprover(
      authentication: Authentication
   ): List<EmployeeValueObject> {
      logger.info("Fetching all approvers")

      val user = userService.fetchUser(authentication)

      val response = purchaseOrderControlService.fetchApprovers(user)

      logger.debug("Fetching all approvers resulted in {}", response)

      return response
   }
}
