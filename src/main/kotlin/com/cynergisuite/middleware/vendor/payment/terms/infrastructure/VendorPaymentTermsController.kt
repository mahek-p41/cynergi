package com.cynergisuite.middleware.vendor.payment.terms.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.audit.infrastructure.AuditAccessControlProvider
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.vendor.payment.terms.VendorPaymentTermsService
import com.cynergisuite.middleware.vendor.payment.terms.VendorPaymentTermsValueObject
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
//TODO Still need the path below if we are using the full path in @Get and @Post and all?
@Controller("/api/vendor/payment/terms")
class VendorPaymentTermsController @Inject constructor(
   private val vendorPaymentTermsService: VendorPaymentTermsService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(VendorPaymentTermsController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("vendorPaymentTerms-fetchOne", accessControlProvider = AuditAccessControlProvider::class)
   @Get(value = "/api/vendor/payment/term/{id}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorPaymentTermsEndpoints"], summary = "Fetch a single VendorPaymentTerms", description = "Fetch a single VendorPaymentTerms by it's system generated primary key", operationId = "vendorPaymentTerms-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorPaymentTermsValueObject::class))]),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The requested VendorPaymentTerms was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @QueryValue("id") id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorPaymentTermsValueObject {
      logger.info("Fetching VendorPaymentTerms by {}", id)

      val user = userService.findUser(authentication)
      val response = vendorPaymentTermsService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching VendorPaymentTerms by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("vendorPaymentTerms-fetchAll", accessControlProvider = AuditAccessControlProvider::class)
   //@Get(uri = "/{auditId}/exception{?pageRequest*}", produces = [APPLICATION_JSON])
   @Get(uri = "/api/vendor/payment/term{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["VendorPaymentTermsEndpoints"], summary = "Fetch a listing of VendorPaymentTermss", description = "Fetch a paginated listing of VendorPaymentTermss based on a parent Audit", operationId = "vendorPaymentTerms-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "auditId", `in` = PATH, description = "The audit for which the listing of exceptions is to be loaded") @QueryValue("auditId") auditId: Long,
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<VendorPaymentTermsValueObject> {
      logger.info("Fetching all details associated with audit {} {}", auditId, pageRequest)

      val user = userService.findUser(authentication)
      val page =  vendorPaymentTermsService.fetchAll(pageRequest, user.myCompany())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(value = "/api/vendor/payment/term", processes = [APPLICATION_JSON])
   @AccessControl("vendorPaymentTerms-create", accessControlProvider = AuditAccessControlProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["VendorPaymentTermsEndpoints"], summary = "Create a single VendorPaymentTerms", description = "Create a single VendorPaymentTerms. The logged in Employee is used for the scannedBy property", operationId = "vendorPaymentTerms-create")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorPaymentTermsValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If the request body is invalid"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The parent Audit was unable to be found or the scanArea was unknown"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun create(
      @Parameter(name = "auditId", `in` = PATH, description = "The audit that is the parent of the exception being created") @QueryValue("auditId") auditId: Long,
      @Body vo: VendorPaymentTermsValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorPaymentTermsValueObject {
      logger.info("Requested Create VendorPaymentTerms {}", vo)

      val user = userService.findUser(authentication)
      val response = vendorPaymentTermsService.create(vo, user.myCompany())

      logger.debug("Requested Create VendorPaymentTerms {} resulted in {}", vo, response)

      return response
   }

   @Put(value = "/api/vendor/payment/term/{id}", processes = [APPLICATION_JSON])
   @AccessControl("vendorPaymentTerms-update", accessControlProvider = AuditAccessControlProvider::class)
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["VendorPaymentTermsEndpoints"], summary = "Update a single VendorPaymentTerms", description = "Update a single VendorPaymentTerms where the update is the addition of a note", operationId = "vendorPaymentTerms-update")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = VendorPaymentTermsValueObject::class))]),
      ApiResponse(responseCode = "400", description = "If request body is invalid"),
      ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
      ApiResponse(responseCode = "404", description = "The requested parent Audit or VendorPaymentTerms was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun update(
      @Parameter(name = "auditId", `in` = PATH, description = "The audit that is the parent of the exception being updated") @QueryValue("auditId") auditId: Long,
      @Body vo: VendorPaymentTermsValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): VendorPaymentTermsValueObject {
      logger.info("Requested Update VendorPaymentTerms {}", vo)

      val user = userService.findUser(authentication)
      val response = vendorPaymentTermsService.update(vo, user.myCompany())

      logger.debug("Requested Update VendorPaymentTerms {} resulted in {}", vo, response)

      return response
   }
}
