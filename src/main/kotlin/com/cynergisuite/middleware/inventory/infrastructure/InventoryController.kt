package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.infrastructure.AlwaysAllowAccessControlProvider
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.inventory.InventoryService
import com.cynergisuite.middleware.inventory.InventoryValueObject
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
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

@Secured(IS_AUTHENTICATED)
@Controller("/api/inventory")
class InventoryController(
   private val userService: UserService,
   private val inventoryService: InventoryService
) {
   private val logger: Logger = LoggerFactory.getLogger(InventoryController::class.java)

   @Throws(AccessException::class)
   @AccessControl("inventory-fetchAll", accessControlProvider = AlwaysAllowAccessControlProvider::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["InventoryEndpoints"], summary = "Fetch a listing of Stores", description = "Fetch a paginated listing of Inventory", operationId = "inventory-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "403", description = "If authentication fails"),
      ApiResponse(responseCode = "204", description = "The the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest") pageRequest: InventoryPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<InventoryValueObject> {
      logger.info("Fetch all inventory for store")

      val user = userService.findUser(authentication)

      logger.info("Requesting inventory available to user {} for page {}", user, pageRequest)

      val pageToRequest = if (pageRequest.storeNumber != null) pageRequest else InventoryPageRequest(pageRequest, user.myLocation().myNumber()!!)
      val page = inventoryService.fetchAll(pageToRequest, user.myCompany(), httpRequest.findLocaleWithDefault())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      } else {
         return page
      }
   }

   @Throws(AccessException::class, NotFoundException::class)
   @AccessControl("inventory-fetchByLookupKey", accessControlProvider = AlwaysAllowAccessControlProvider::class)
   @Get(uri = "/{lookupKey}", produces = [APPLICATION_JSON])
   @Operation(tags = ["InventoryEndpoints"], summary = "Fetch an Inventory item by lookupKey", description = "Fetch an Inventory item by lookupKey", operationId = "inventory-fetchByLookupKey")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = InventoryValueObject::class))]),
      ApiResponse(responseCode = "403", description = "If authentication fails"),
      ApiResponse(responseCode = "404", description = "If the barcode was unable to be located"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchByBarcode(
      @Parameter(name = "lookupKey", `in` = PATH, required = false) @QueryValue("lookupKey") lookupKey: String,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): InventoryValueObject {
      logger.info("Fetching Inventory by barcode {}", lookupKey)

      val user = userService.findUser(authentication)

      return inventoryService.fetchByLookupKey(lookupKey, user.myCompany(), httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(lookupKey)
   }
}
