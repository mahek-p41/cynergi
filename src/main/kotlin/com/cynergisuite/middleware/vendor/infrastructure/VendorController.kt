package com.cynergisuite.middleware.vendor.infrastructure

import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.vendor.VendorService
import com.cynergisuite.middleware.vendor.VendorValueObject
import io.micronaut.http.HttpRequest
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/vendor")
class VendorController @Inject constructor(
   private val vendorService: VendorService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorEndpoints"], summary = "Fetch a single Vendor", description = "Fetch a single Vendor by it's system generated primary key", operationId = "vendor-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorValueObject::class))]),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The requested VendorPaymentTerm was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @QueryValue("id") id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorValueObject {
      logger.info("Fetching Vendor by {}", id)

      val user = userService.findUser(authentication)
      val response = vendorService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Vendor by {} resulted in", id, response)

      return response
   }

   /*
   @Throws(PageOutOfBoundsException::class)
   @AccessControl("vendor-fetchAll", accessControlProvider = VendorAccessControlProvider::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorEndpoints"], summary = "Fetch a listing of Vendors", description = "Fetch a paginated listing of Vendors", operationId = "vendor-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If there are Vendors that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The requested Vendor was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @Valid @QueryValue("pageRequest") pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<VendorValueObject> {
      logger.info("Fetching all vendors {}", pageRequest)

      val user = authenticationService.findUser(authentication)
      val page =  vendorService.fetchAll(pageRequest, user.myDataset(), httpRequest.findLocaleWithDefault())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }
   */

   @Post(processes = [APPLICATION_JSON])
   @AccessControl("vendor-create")
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["VendorEndpoints"], summary = "Create a single Vendor", description = "Create a single Vendor. The logged in Employee is used for the scannedBy property", operationId = "vendor-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If the request body is invalid"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The vendor was unable to be found or the scanArea was unknown"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun create(
      @Body vo: VendorValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorValueObject {
      logger.debug("Requested Create Vendor {}", vo)

      val user = userService.findUser(authentication)
      val response = vendorService.create(vo, user.myCompany())

      logger.debug("Requested Create Vendor {} resulted in {}", vo, response)

      return response
   }

   /*
   @Put(processes = [APPLICATION_JSON])
   @AccessControl("vendor-completeOrCancel", accessControlProvider = VendorAccessControlProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["VendorEndpoints"], summary = "Update a single Vendor", description = "This operation is useful for changing the state of the Vendor.  Depending on the state being changed the logged in employee will be used for the appropriate fields", operationId = "vendor-completeOrCancel")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If successfully able to update Vendor", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The requested Vendor was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun addNoteCompleteOrCancel(
      @Body vendor: VendorUpdateValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorValueObject {
      logger.info("Requested Vendor status change or note  {}", vendor)

      val user = authenticationService.findUser(authentication)
      val response = vendorService.completeOrCancel(vendor, user, httpRequest.findLocaleWithDefault())

      logger.debug("Requested Update Vendor {} resulted in {}", vendor, response)

      return response
   }

    */
}
