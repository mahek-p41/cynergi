package com.cynergisuite.middleware.audit.detail.scan.area.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import io.micronaut.core.version.annotation.Version
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.ValidationException

@Secured(SecurityRule.IS_AUTHENTICATED)
// require access to this controller to at the very least be authenticated
@Controller("/api/audit/detail/scan-area")
class AuditScanAreaController @Inject constructor(
   private val auditScanAreaService: AuditScanAreaService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditScanAreaController::class.java)

   @Get(uri = "/{id:[0-9]+}")
   @Operation(tags = ["AuditScanAreaEndpoints"], description = "Fetch a Scan Area by ID", operationId = "auditDetailScanArea-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AuditScanAreaDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested Scan Area was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Scan Area with", `in` = ParameterIn.PATH) @QueryValue("id")
      id: Long,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): AuditScanAreaDTO {
      val user = userService.findUser(authentication)
      val found = auditScanAreaService.fetchOne(user.myCompany(), id) ?: throw NotFoundException(id)

      logger.debug("Fetch Audit Scan Areas by ID resulted in {}", found)

      return found
   }

   @Deprecated("This resource is used to support the mobile app’s existing expectations for the shape of the response.")
   @Get
   @Operation(tags = ["AuditScanAreaEndpoints"], description = "Fetch a listing of supported audit detail Scan Areas", operationId = "auditDetailScanArea-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AuditScanAreaDTO::class))]),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllByUser(
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): List<AuditScanAreaDTO> {
      val user = userService.findUser(authentication)
      val areas = auditScanAreaService.fetchAll(user)

      logger.debug("Listing of Audit Scan Areas resulted in {}", areas)

      return areas
   }

   @Version("2")
   @Get(uri = "{?pageRequest*,storeId:[0-9]+}")
   @Operation(tags = ["AuditScanAreaEndpoints"], description = "Fetch a paginated listing of supported audit detail Scan Areas", operationId = "auditDetailScanArea-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAllByStore(
      @Parameter(description = "Primary Key to lookup the Scan Area with", `in` = ParameterIn.PATH) @QueryValue("storeId")
      storeId: Long?,
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ): Page<AuditScanAreaDTO> {
      logger.info("Fetching all Scan Areas by store {}", pageRequest)

      val user = userService.findUser(authentication)
      val page = auditScanAreaService.fetchAll(user.myCompany(), storeId!!, pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      logger.debug("Listing of Scan Areas resulted in {}", page)

      return page
   }

   @Post(processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditScanAreaEndpoints"], description = "Create a Scan Area.", operationId = "auditDetailScanArea-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save Scan Area", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AuditScanAreaDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun save(
      @Body @Valid
      auditScanAreaDTO: AuditScanAreaDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditScanAreaDTO {
      logger.info("Requested Save AuditScanArea {}", auditScanAreaDTO)

      val user = userService.findUser(authentication)
      val response = auditScanAreaService.create(auditScanAreaDTO, user.myCompany())

      logger.debug("Requested Save AuditScanArea {} resulted in {}", auditScanAreaDTO, response)

      return response
   }

   @Put(uri = "/{id}", processes = [MediaType.APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditScanAreaEndpoints"], summary = "Create a single auditScanArea", description = "Create a single auditScanArea.", operationId = "auditDetailScanArea-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update AuditScanArea", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = AuditScanAreaDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested AuditScanArea was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: Long,
      @Body @Valid
      auditScanAreaDTO: AuditScanAreaDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditScanAreaDTO {
      logger.info("Requested Update AuditScanArea {}", auditScanAreaDTO)

      val user = userService.findUser(authentication)
      val response = auditScanAreaService.update(id, auditScanAreaDTO, user.myCompany())

      logger.debug("Requested Update AuditScanArea {} resulted in {}", auditScanAreaDTO, response)

      return response
   }
}
