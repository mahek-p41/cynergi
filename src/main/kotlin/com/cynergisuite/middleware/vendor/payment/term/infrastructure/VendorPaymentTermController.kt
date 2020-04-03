package com.cynergisuite.middleware.vendor.payment.term.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.audit.infrastructure.AuditAccessControlProvider
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermService
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermValueObject
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
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/vendor/payment/term")
class VendorPaymentTermController @Inject constructor(
   private val vendorPaymentTermService: VendorPaymentTermService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorPaymentTermController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorPaymentTermEndpoints"], summary = "Fetch a single VendorPaymentTerm", description = "Fetch a single VendorPaymentTerm by it's system generated primary key", operationId = "vendorPaymentTerm-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorPaymentTermValueObject::class))]),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The requested VendorPaymentTerm was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @QueryValue("id") id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorPaymentTermValueObject {
      logger.info("Fetching VendorPaymentTerm by {}", id)

      val user = userService.findUser(authentication)
      val response = vendorPaymentTermService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching VendorPaymentTerm by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorPaymentTermEndpoints"], summary = "Fetch a listing of VendorPaymentTerm", description = "Fetch a paginated listing of VendorPaymentTerm based on a parent Audit", operationId = "vendorPaymentTerm-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The requested vendor payment term was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<VendorPaymentTermValueObject> {
      logger.info("Fetching all details associated with vendor payment term {}", pageRequest)

      val user = userService.findUser(authentication)
      val page =  vendorPaymentTermService.fetchAll(pageRequest, user.myCompany())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["VendorPaymentTermEndpoints"], summary = "Create a single VendorPaymentTerm", description = "Create a single VendorPaymentTerm. The logged in Employee is used for the scannedBy property", operationId = "vendorPaymentTerm-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorPaymentTermValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If the request body is invalid"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The vendor payment term was unable to be found or the scanArea was unknown"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun create(
      @Body vo: VendorPaymentTermValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorPaymentTermValueObject {
      logger.info("Requested Create VendorPaymentTerm {}", vo)

      val user = userService.findUser(authentication)
      val response = vendorPaymentTermService.create(vo, user.myCompany())

      logger.debug("Requested Create VendorPaymentTerm {} resulted in {}", vo, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @AccessControl("vendorPaymentTerm-update", accessControlProvider = AuditAccessControlProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["VendorPaymentTermEndpoints"], summary = "Update a single VendorPaymentTerm", description = "Update a single VendorPaymentTerm where the update is the addition of a note", operationId = "vendorPaymentTerm-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorPaymentTermValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If request body is invalid"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The requested VendorPaymentTerm was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the vendor payment term being updated") @QueryValue("id") id: Long,
      @Body vo: VendorPaymentTermValueObject,
      httpRequest: HttpRequest<*>
   ): VendorPaymentTermValueObject {
      logger.info("Requested Update VendorPaymentTerm {}", vo)

      val response = vendorPaymentTermService.update(id, vo)

      logger.debug("Requested Update VendorPaymentTerm {} resulted in {}", vo, response)

      return response
   }
}
