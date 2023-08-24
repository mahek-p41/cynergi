package com.cynergisuite.middleware.vendor.payment.term.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermDTO
import com.cynergisuite.middleware.vendor.payment.term.VendorPaymentTermService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
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
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
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
@Controller("/api/vendor/payment/term")
class VendorPaymentTermController @Inject constructor(
   private val vendorPaymentTermService: VendorPaymentTermService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorPaymentTermController::class.java)

   // TODO security validation that this vendor_payment_term id is owned by the same company as user that is logged in
   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorPaymentTermEndpoints"], summary = "Fetch a single VendorPaymentTerm", description = "Fetch a single VendorPaymentTerm by it's system generated primary key", operationId = "vendorPaymentTerm-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorPaymentTermDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested VendorPaymentTerm was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorPaymentTermDTO {
      logger.info("Fetching VendorPaymentTerm by {}", id)

      val user = userService.fetchUser(authentication)
      val response = vendorPaymentTermService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching VendorPaymentTerm by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorPaymentTermEndpoints"], summary = "Fetch a listing of VendorPaymentTerms", description = "Fetch a paginated listing of VendorPaymentTerms including associated schedule records", operationId = "vendorPaymentTerm-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Vendor Payment Term was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<VendorPaymentTermDTO> {
      logger.info("Fetching all vendor payment terms {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = vendorPaymentTermService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Secured("MCFVENDTERMADD")
   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["VendorPaymentTermEndpoints"], summary = "Create a single VendorPaymentTerm", description = "Create a single VendorPaymentTerm. The logged in Employee is used for the scannedBy property", operationId = "vendorPaymentTerm-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorPaymentTermDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The vendor payment term was unable to be found or the scanArea was unknown"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      vo: VendorPaymentTermDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorPaymentTermDTO {
      logger.debug("Requested Create VendorPaymentTerm {}", vo)

      val user = userService.fetchUser(authentication)
      val response = vendorPaymentTermService.create(vo, user.myCompany())

      logger.debug("Requested Create VendorPaymentTerm {} resulted in {}", vo, response)

      return response
   }

   // TODO security validation that this vendor_payment_term id is owned by the same company as user that is logged in

   @Secured("MCFVENDTERMCHG")
   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["VendorPaymentTermEndpoints"], summary = "Update a single VendorPaymentTerm", description = "Update a single VendorPaymentTerm where the update is the addition of a note", operationId = "vendorPaymentTerm-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorPaymentTermDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested VendorPaymentTerm was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the vendor payment term being updated") @QueryValue("id")
      id: UUID,
      @Body @Valid
      vo: VendorPaymentTermDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorPaymentTermDTO {
      logger.info("Requested Update VendorPaymentTerm {}", vo)

      val user = userService.fetchUser(authentication)
      val response = vendorPaymentTermService.update(id, vo, user.myCompany())

      logger.debug("Requested Update VendorPaymentTerm {} resulted in {}", vo, response)

      return response
   }

   @Secured("MCFVENDTERMDEL")
   @Delete(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [MediaType.APPLICATION_JSON])
   @AccessControl
   @Operation(tags = ["VendorPaymentTermEndpoints"], summary = "Delete a vendor payment term", description = "Delete a single vendor payment term", operationId = "vendorPaymentTerm-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the vendor payment term was able to be deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "409", description = "If the vendor payment term is still referenced from other tables"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete vendor payment term", authentication)

      val user = userService.fetchUser(authentication)

      return vendorPaymentTermService.delete(id, user.myCompany())
   }
}
