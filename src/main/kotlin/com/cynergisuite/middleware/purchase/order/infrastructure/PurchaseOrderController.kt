package com.cynergisuite.middleware.purchase.order.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.purchase.order.PurchaseOrderDTO
import com.cynergisuite.middleware.purchase.order.PurchaseOrderService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/purchase-order")
class PurchaseOrderController @Inject constructor(
   private val purchaseOrderService: PurchaseOrderService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["PurchaseOrderEndpoints"], summary = "Fetch a single PurchaseOrder", description = "Fetch a single PurchaseOrder by it's system generated primary key", operationId = "purchaseOrder-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested PurchaseOrder was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Valid @QueryValue("id")
      id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): PurchaseOrderDTO {
      logger.info("Fetching PurchaseOrder by {}", id)

      val user = userService.findUser(authentication)
      val response = purchaseOrderService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching PurchaseOrder by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["PurchaseOrderEndpoints"], summary = "Fetch a listing of PurchaseOrders", description = "Fetch a paginated listing of PurchaseOrder", operationId = "purchaseOrder-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested PurchaseOrder was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<PurchaseOrderDTO> {
      logger.info("Fetching all PurchaseOrders {}", pageRequest)

      val user = userService.findUser(authentication)
      val page = purchaseOrderService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["PurchaseOrderEndpoints"], summary = "Create a single PurchaseOrder", description = "Create a single PurchaseOrder", operationId = "purchaseOrder-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The PurchaseOrder was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: PurchaseOrderDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): PurchaseOrderDTO {
      logger.debug("Requested Create PurchaseOrder {}", dto)

      val user = userService.findUser(authentication)
      val response = purchaseOrderService.create(dto, user.myCompany())

      logger.debug("Requested Create PurchaseOrder {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["PurchaseOrderEndpoints"], summary = "Update a single PurchaseOrder", description = "Update a single PurchaseOrder", operationId = "purchaseOrder-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested PurchaseOrder was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the PurchaseOrder being updated")
      @QueryValue("id")
      id: Long,
      @Body @Valid
      dto: PurchaseOrderDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): PurchaseOrderDTO {
      logger.info("Requested Update PurchaseOrder {}", dto)

      val user = userService.findUser(authentication)
      val response = purchaseOrderService.update(id, dto, user.myCompany())

      logger.debug("Requested Update PurchaseOrder {} resulted in {}", dto, response)

      return response
   }

   @Delete(uri = "/{id:[0-9]+}")
   @Operation(tags = ["PurchaseOrderEndpoints"], summary = "Delete a single purchase order", description = "Deletes a purchase order based on passed id", operationId = "purchaseOrder-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the purchase order record was deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling the endpoint does not have permission"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: Long,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete purchase order", authentication)

      val user = userService.findUser(authentication)

      return purchaseOrderService.delete(id, user.myCompany())
   }
}
