package com.cynergisuite.middleware.vendor.rebate.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.vendor.rebate.RebateDTO
import com.cynergisuite.middleware.vendor.rebate.RebateService
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
import javax.inject.Inject
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/vendor/rebate")
class RebateController @Inject constructor(
   private val rebateService: RebateService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(RebateController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["RebateEndpoints"], summary = "Fetch a single Rebate", description = "Fetch a single Rebate by it's system generated primary key", operationId = "rebate-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RebateDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Rebate was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Valid @QueryValue("id")
      id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): RebateDTO {
      logger.info("Fetching Rebate by {}", id)

      val user = userService.fetchUser(authentication)
      val response = rebateService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Rebate by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["RebateEndpoints"], summary = "Fetch a listing of Rebates", description = "Fetch a paginated listing of Rebate", operationId = "rebate-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Rebate was unable to be found, or the result is empty"),
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
   ): Page<RebateDTO> {
      logger.info("Fetching all rebates {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = rebateService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["RebateEndpoints"], summary = "Create a single Rebate", description = "Create a single Rebate", operationId = "rebate-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RebateDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The rebate was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: RebateDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): RebateDTO {
      logger.debug("Requested Create Rebate {}", dto)

      val user = userService.fetchUser(authentication)
      val response = rebateService.create(dto, user.myCompany())

      logger.debug("Requested Create Rebate {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["RebateEndpoints"], summary = "Update a single Rebate", description = "Update a single Rebate", operationId = "rebate-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RebateDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Rebate was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the rebate being updated")
      @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: RebateDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): RebateDTO {
      logger.info("Requested Update Rebate {}", dto)

      val user = userService.fetchUser(authentication)
      val response = rebateService.update(id, dto, user.myCompany())

      logger.debug("Requested Update Rebate {} resulted in {}", dto, response)

      return response
   }

   @Post(uri = "/{rebateId:[0-9a-fA-F\\-]+}/vendor", processes = [APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["RebateToVendorEndpoints"], summary = "Assign a vendor to rebate", description = "Assign a vendor to rebate.", operationId = "rebate-assignVendor")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to assign a vendor to rebate", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = SimpleIdentifiableDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun assignVendor(
      @QueryValue("rebateId") rebateId: UUID,
      @Body vendorDTO: SimpleIdentifiableDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ) {
      logger.info("Requested assign a Vendor {} to Rebate", vendorDTO)

      val user = userService.fetchUser(authentication)

      rebateService.assignVendorToRebate(rebateId, vendorDTO, user.myCompany())
   }

   @Delete(uri = "/{rebateId:[0-9a-fA-F\\-]+}/vendor/{vendorId:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @AccessControl
   @Operation(tags = ["RebateToVendorEndpoints"], summary = "Disassociate a vendor from rebate", description = "Disassociate a vendor from rebate", operationId = "rebate-disassociateVendor")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the Rebate To Vendor was able to be deleted", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = RebateDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Rebate was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun disassociateVendor(
      @QueryValue("rebateId") rebateId: UUID,
      @QueryValue("vendorId") vendorId: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.info("Requested disassociate a Vendor {} from Rebate {}", vendorId, rebateId)

      val user = userService.fetchUser(authentication)

      rebateService.disassociateVendorFromRebate(rebateId, vendorId, user.myCompany())
   }
}
