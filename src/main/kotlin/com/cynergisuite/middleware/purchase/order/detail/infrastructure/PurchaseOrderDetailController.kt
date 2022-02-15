package com.cynergisuite.middleware.purchase.order.detail.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.purchase.order.detail.PurchaseOrderDetailDTO
import com.cynergisuite.middleware.purchase.order.detail.PurchaseOrderDetailService
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
import java.util.UUID
import jakarta.inject.Inject
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/purchase-order/detail")
class PurchaseOrderDetailController @Inject constructor(
   private val purchaseOrderDetailService: PurchaseOrderDetailService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(PurchaseOrderDetailController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["PurchaseOrderDetailEndpoints"], summary = "Fetch a single PurchaseOrderDetail", description = "Fetch a single PurchaseOrderDetail by its system generated primary key", operationId = "purchaseOrderDetail-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderDetailDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested PurchaseOrderDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Valid @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): PurchaseOrderDetailDTO {
      logger.info("Fetching PurchaseOrderDetail by {}", id)

      val user = userService.fetchUser(authentication)
      val response = purchaseOrderDetailService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching PurchaseOrderDetail by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["PurchaseOrderDetailEndpoints"], summary = "Fetch a listing of PurchaseOrderDetails", description = "Fetch a paginated listing of PurchaseOrderDetail", operationId = "purchaseOrderDetail-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested PurchaseOrderDetail was unable to be found, or the result is empty"),
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
   ): Page<PurchaseOrderDetailDTO> {
      logger.info("Fetching all PurchaseOrderDetails {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = purchaseOrderDetailService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["PurchaseOrderDetailEndpoints"], summary = "Create a single PurchaseOrderDetail", description = "Create a single PurchaseOrderDetail", operationId = "purchaseOrderDetail-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The PurchaseOrderDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: PurchaseOrderDetailDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): PurchaseOrderDetailDTO {
      logger.debug("Requested Create PurchaseOrderDetail {}", dto)

      val user = userService.fetchUser(authentication)
      val response = purchaseOrderDetailService.create(dto, user.myCompany())

      logger.debug("Requested Create PurchaseOrderDetail {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["PurchaseOrderDetailEndpoints"], summary = "Update a single PurchaseOrderDetail", description = "Update a single PurchaseOrderDetail", operationId = "purchaseOrderDetail-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = PurchaseOrderDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested PurchaseOrderDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the PurchaseOrderDetail being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: PurchaseOrderDetailDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): PurchaseOrderDetailDTO {
      logger.info("Requested Update PurchaseOrderDetail {}", dto)

      val user = userService.fetchUser(authentication)
      val response = purchaseOrderDetailService.update(id, dto, user.myCompany())

      logger.debug("Requested Update PurchaseOrderDetail {} resulted in {}", dto, response)

      return response
   }

   @Delete(uri = "/{id:[0-9a-fA-F\\-]+}")
   @Operation(tags = ["PurchaseOrderDetailEndpoints"], summary = "Delete a single purchase order detail", description = "Deletes a purchase order detail based on passed id", operationId = "purchaseOrderDetail-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the purchase order detail record was deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling the endpoint does not have permission"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete purchase order detail", authentication)

      val user = userService.fetchUser(authentication)

      return purchaseOrderDetailService.delete(id, user.myCompany())
   }
}
