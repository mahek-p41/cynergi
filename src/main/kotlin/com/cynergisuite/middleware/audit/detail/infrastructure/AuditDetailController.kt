package com.cynergisuite.middleware.audit.detail.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.audit.detail.AuditDetailCreateUpdateDTO
import com.cynergisuite.middleware.audit.detail.AuditDetailEntity
import com.cynergisuite.middleware.audit.detail.AuditDetailService
import com.cynergisuite.middleware.audit.detail.AuditDetailValidator
import com.cynergisuite.middleware.audit.detail.AuditDetailValueObject
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaDTO
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
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
@Controller("/api/audit")
class AuditDetailController @Inject constructor(
   private val auditDetailService: AuditDetailService,
   private val auditDetailValidator: AuditDetailValidator,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(AuditDetailController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/detail/{id}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditDetailEndpoints"], summary = "Fetch a single AuditDetail", description = "Fetch a single AuditDetail by it's system generated primary key", operationId = "auditDetail-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditDetailValueObject::class))]),
         ApiResponse(responseCode = "404", description = "The requested AuditDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditDetailValueObject {
      logger.info("Fetching AuditDetail by {}", id)

      val user = userService.fetchUser(authentication)
      val response = auditDetailService.fetchById(id = id, company = user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching AuditDetail by {} resulted in", id, response)

      return transformEntity(response)
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/{auditId}/detail{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["AuditDetailEndpoints"], summary = "Fetch a listing of AuditDetails", description = "Fetch a paginated listing of AuditDetails based on a parent Audit", operationId = "auditDetail-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "auditId", `in` = ParameterIn.PATH, description = "The audit for which the listing of details is to be loaded") @QueryValue("auditId")
      auditId: UUID,
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<AuditDetailValueObject> {
      logger.info("Fetching all details associated with audit {} {}", auditId, pageRequest)

      val user = userService.fetchUser(authentication)
      val page = auditDetailService.fetchAll(auditId, user.myCompany(), pageRequest)
         .toPage { transformEntity(it) }

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(uri = "/{auditId}/detail", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditDetailEndpoints"], summary = "Create a single AuditDetail", description = "Create a single AuditDetail. The logged in Employee is used for the scannedBy property", operationId = "auditDetail-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditDetailValueObject::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The parent Audit was unable to be found, or the scanArea was unknown"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Parameter(name = "auditId", `in` = ParameterIn.PATH, description = "The audit for which the listing of details is to be loaded") @QueryValue("auditId")
      auditId: UUID,
      @Valid @Body
      vo: AuditDetailCreateUpdateDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): HttpResponse<AuditDetailValueObject> {
      logger.info("Requested Create AuditDetail {}", vo)

      val user = userService.fetchUser(authentication)
      val existingDetail = auditDetailValidator.validateDuplicateDetail(auditId, vo, user)

      return if (existingDetail != null) {
         HttpResponse.notModified()
      } else {
         val detailToCreate = auditDetailValidator.validateCreate(auditId, user, vo)
         val response = auditDetailService.create(detailToCreate, user)

         logger.debug("Requested Create AuditDetail {} resulted in {}", vo, response)

         HttpResponse.created(transformEntity(response))
      }
   }

   @Put(uri = "/{auditId}/detail/{auditDetailId}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["AuditDetailEndpoints"], summary = "Modify a single AuditDetail", description = "Modify a single AuditDetail. The logged in Employee is used for the scannedBy property", operationId = "auditDetail-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = AuditDetailValueObject::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The parent Audit was unable to be found, or the scanArea was unknown"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "auditId", `in` = ParameterIn.PATH, description = "The audit for which the listing of details is to be loaded") @QueryValue("auditId")
      auditId: UUID,
      @Parameter(name = "auditDetailId", `in` = ParameterIn.PATH, description = "The audit detail id") @QueryValue("auditDetailId")
      auditDetailId: UUID,
      @Valid @Body
      vo: AuditDetailCreateUpdateDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): AuditDetailValueObject {
      logger.info("Requested Update AuditDetail {}", vo)

      val user = userService.fetchUser(authentication)
      val existingDetail = auditDetailValidator.validateUpdate(auditId, user, vo)
      val response = auditDetailService.update(existingDetail, user)

      logger.debug("Requested Update AuditDetail {} resulted in {}", existingDetail, response)

      return transformEntity(response)
   }

   private fun transformEntity(auditDetail: AuditDetailEntity): AuditDetailValueObject {
      return AuditDetailValueObject(entity = auditDetail, auditScanArea = AuditScanAreaDTO(auditDetail.scanArea))
   }
}
