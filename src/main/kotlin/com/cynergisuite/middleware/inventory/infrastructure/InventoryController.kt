package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.domain.InventoryInquiryFilterRequest
import com.cynergisuite.domain.InventoryInvoiceFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.extensions.toUuid
import com.cynergisuite.middleware.authentication.AccessException
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.inventory.AssociateInventoryToInvoiceDTO
import com.cynergisuite.middleware.inventory.InventoryDTO
import com.cynergisuite.middleware.inventory.InventoryInquiryDTO
import com.cynergisuite.middleware.inventory.InventoryInvoiceDTO
import com.cynergisuite.middleware.inventory.InventoryService
import com.cynergisuite.middleware.json.view.Full
import com.cynergisuite.middleware.json.view.InventoryApp
import com.fasterxml.jackson.annotation.JsonView
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_FORM_URLENCODED
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/inventory")
class InventoryController(
   private val userService: UserService,
   private val inventoryService: InventoryService
) {
   private val logger: Logger = LoggerFactory.getLogger(InventoryController::class.java)

   @JsonView(value = [Full::class])
   @Throws(AccessException::class)
   @Get(uri = "/all{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["InventoryEndpoints"], summary = "Fetch a listing of inventory", description = "Fetch a paginated listing of Inventory", operationId = "inventory-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "403", description = "If authentication fails"),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest") @Valid
      pageRequest: InventoryPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<InventoryDTO> {
      logger.info("Fetch all inventory for store")

      val user = userService.fetchUser(authentication)

      logger.info("Requesting inventory available to user {} for page {}", user, pageRequest)

      val pageToRequest = if (pageRequest.storeNumber != null) pageRequest else InventoryPageRequest(pageRequest, user.myLocation().myNumber())
      val page = inventoryService.fetchAll(pageToRequest, user.myCompany(), httpRequest.findLocaleWithDefault())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      } else {
         return page
      }
   }

   @JsonView(value = [InventoryApp::class])
   @Throws(AccessException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["InventoryEndpoints"], summary = "Fetch a listing of inventory for use by the inventory auditing app", description = "Fetch a paginated listing of Inventory for audit app", operationId = "inventory-fetchAll-v2")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "403", description = "If authentication fails"),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllInventoryApp(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest") @Valid
      pageRequest: InventoryPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<InventoryDTO> {
      return fetchAll(pageRequest, authentication, httpRequest)
   }

   @Deprecated("The is a deprecated endpoint to support the old scanning app.")
   @Throws(AccessException::class, NotFoundException::class)
   @Get(uri = "/{lookupKey}", produces = [APPLICATION_JSON])
   @Operation(tags = ["InventoryEndpoints"], summary = "Fetch an Inventory item by lookupKey", description = "Fetch an Inventory item by lookupKey", operationId = "inventory-fetchByLookupKey")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = InventoryDTO::class))]),
         ApiResponse(responseCode = "403", description = "If authentication fails"),
         ApiResponse(responseCode = "404", description = "If the barcode was unable to be located"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchByBarcode(
      @Parameter(name = "lookupKey", `in` = ParameterIn.PATH, required = false) @QueryValue("lookupKey")
      lookupKey: String,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): InventoryDTO {
      logger.info("Fetching Inventory by barcode {}", lookupKey)

      val user = userService.fetchUser(authentication)

      return inventoryService.fetchByLookupKey(lookupKey, user.myCompany(), httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(lookupKey)
   }

   @Throws(AccessException::class, NotFoundException::class)
   @Get(uri = "/lookup", produces = [APPLICATION_JSON, APPLICATION_FORM_URLENCODED], consumes = [APPLICATION_JSON, APPLICATION_FORM_URLENCODED])
   @Operation(tags = ["InventoryEndpoints"], summary = "Fetch an Inventory item by lookup key", description = "Fetch an Inventory item by lookup key", operationId = "inventory-fetchByLookupKey-v2")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = InventoryDTO::class))]),
         ApiResponse(responseCode = "403", description = "If authentication fails"),
         ApiResponse(responseCode = "404", description = "If the lookup key was unable to be located"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchByLookupKey(
      @Parameter(name = "key", `in` = QUERY, required = true) @QueryValue("key")
      lookupKey: String,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): InventoryDTO {
      logger.info("Fetching Inventory by lookup key {}", lookupKey)

      val user = userService.fetchUser(authentication)

      return inventoryService.fetchByLookupKey(lookupKey, user.myCompany(), httpRequest.findLocaleWithDefault()) ?: throw NotFoundException(lookupKey)
   }

   @Throws(AccessException::class)
   @Get(uri = "/inquiry{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["InventoryEndpoints"], summary = "Fetch an Inventory Inquiry", description = "Fetch an Account Payable Inventory Inquiry", operationId = "inventory-inquiry")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "403", description = "If authentication fails"),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun inquiry(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false) @QueryValue("filterRequest") @Valid
      filterRequest: InventoryInquiryFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<InventoryInquiryDTO> {
      logger.info("Fetching Account Payable Inventory Inquiry {}", filterRequest)

      val user = userService.fetchUser(authentication)

      val page = inventoryService.inquiry(user.myCompany(), filterRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(filterRequest)
      } else {
         return page
      }
   }

   @Throws(AccessException::class)
   @Get(uri = "/invoice{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["InventoryEndpoints"], summary = "Fetch an Inventory Inquiry", description = "Fetch an Account Payable Inventory Inquiry", operationId = "inventory-inquiry")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "403", description = "If authentication fails"),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun invoiceInventory(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false) @QueryValue("filterRequest") @Valid
      filterRequest: InventoryInvoiceFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<InventoryDTO> {
      logger.info("Fetching Account Payable Inventory Inquiry {}", filterRequest)

      val user = userService.fetchUser(authentication)

      return inventoryService.invoice(user.myCompany(), filterRequest)
   }

   @Throws(AccessException::class)
   @Get(uri = "/invoice/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["InventoryEndpoints"], summary = "Fetch an Inventory Inquiry", description = "Fetch an Account Payable Inventory Inquiry", operationId = "inventory-inquiry")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "403", description = "If authentication fails"),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchByInvoice(
      @QueryValue("id")
      invoiceId: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<InventoryDTO>? {
      logger.info("Fetching Account Payable Inventory by Invoice {}", invoiceId)

      val user = userService.fetchUser(authentication)

      return inventoryService.fetchByInvoiceId(invoiceId, user.myCompany(), httpRequest.findLocaleWithDefault())
   }

   @Put(value = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["InventoryEndpoints"], summary = "Update a single Inventory", description = "Update a single Inventory", operationId = "inventory-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = InventoryDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Recurring Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )

   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the Inventory being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: InventoryDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): InventoryDTO {
      logger.info("Requested Update Account Payable Recurring Invoice {}", dto)

      val user = userService.fetchUser(authentication)
      val response = inventoryService.update(dto, user.myCompany(), httpRequest.findLocaleWithDefault())

      //logger.debug("Requested Update Account Payable Recurring Invoice {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/invoice/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["InventoryEndpoints"], summary = "Detach/Attach Inventory to an Invoice", description = "Detach/Attach Inventory to an Invoice", operationId = "inventory-associateInventoryToInvoice")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = InventoryDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Recurring Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )

   fun associateInventoryToInvoice(
      @Parameter(name = "id", `in` = PATH, description = "The id for the invoice to attach inventory")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: AssociateInventoryToInvoiceDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Requested Update Account Payable Recurring Invoice {}", dto)

      val user = userService.fetchUser(authentication)
      val response = inventoryService.associateInventoryToInvoice(id, dto, user.myCompany(), httpRequest.findLocaleWithDefault())

      //logger.debug("Requested Update Account Payable Recurring Invoice {} resulted in {}", dto, response)

   }

   @Put(value = "/invoice{?filterRequest*}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["InventoryEndpoints"], summary = "Update a single Inventory", description = "Update a single Inventory", operationId = "inventory-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = InventoryDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Recurring Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun updateByCriteria(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false) @QueryValue("filterRequest") @Valid
      filterRequest: InventoryInvoiceFilterRequest,
      @Body
      invoiceId: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<InventoryDTO> {
      logger.info("Requested Update Account Payable Recurring Invoice {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val response = inventoryService.updateCriteria(filterRequest, invoiceId, user.myCompany(), httpRequest.findLocaleWithDefault())

      //logger.debug("Requested Update Account Payable Recurring Invoice {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/invoice{?filterRequest*}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["InventoryEndpoints"], summary = "Update a single Inventory", description = "Update a single Inventory", operationId = "inventory-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = InventoryDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Account Payable Recurring Invoice was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun removeByCriteria(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false) @QueryValue("filterRequest") @Valid
      filterRequest: InventoryInvoiceFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): List<InventoryDTO> {
      logger.info("Requested Update Account Payable Recurring Invoice {}", filterRequest)

      val user = userService.fetchUser(authentication)
      val response = inventoryService.removeCriteria(filterRequest, user.myCompany(), httpRequest.findLocaleWithDefault())

      //logger.debug("Requested Update Account Payable Recurring Invoice {} resulted in {}", dto, response)

      return response
   }
}
