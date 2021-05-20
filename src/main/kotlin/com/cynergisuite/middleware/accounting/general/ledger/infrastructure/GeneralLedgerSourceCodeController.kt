package com.cynergisuite.middleware.accounting.general.ledger.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceCodeService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
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
@Controller("/api/general-ledger/source-code")
class GeneralLedgerSourceCodeController @Inject constructor(
   private val generalLedgerSourceCodeService: GeneralLedgerSourceCodeService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerSourceCodeController::class.java)

   @Throws(NotFoundException::class)
   @Get(value = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerSourceCodeEndpoints"], summary = "Fetch a single GeneralLedgerSourceCode", description = "Fetch a single GeneralLedgerSourceCode by it's system generated primary key", operationId = "generalLedgerSourceCode-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerSourceCodeDTO::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerSourceCode was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerSourceCodeDTO {
      logger.info("Fetching GeneralLedgerSourceCode by {}", id)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerSourceCodeService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching GeneralLedgerSourceCode by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerSourceCodeEndpoints"], summary = "Fetch a listing of GeneralLedgerSourceCodes", description = "Fetch a paginated listing of GeneralLedgerSourceCodes", operationId = "generalLedgerSourceCode-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested GeneralLedgerSourceCode was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<GeneralLedgerSourceCodeDTO> {
      logger.info("Fetching all GeneralLedgerSourceCodes {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = generalLedgerSourceCodeService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerSourceCodeEndpoints"], summary = "Create a single GeneralLedgerSourceCode", description = "Create a single GeneralLedgerSourceCode", operationId = "generalLedgerSourceCode-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerSourceCodeDTO::class))]),
         ApiResponse(responseCode = "400", description = "If the request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The GeneralLedgerSourceCode was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerSourceCodeDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerSourceCodeDTO {
      logger.debug("Requested Create GeneralLedgerSourceCode {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerSourceCodeService.create(dto, user.myCompany())

      logger.debug("Requested Create GeneralLedgerSourceCode {} resulted in {}", dto, response)

      return response
   }

   @Put(value = "/{id}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerSourceCodeEndpoints"], summary = "Update a single GeneralLedgerSourceCode", description = "Update a single GeneralLedgerSourceCode", operationId = "generalLedgerSourceCode-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerSourceCodeDTO::class))]),
         ApiResponse(responseCode = "400", description = "If request body is invalid"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerSourceCode was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Parameter(name = "id", `in` = PATH, description = "The id for the GeneralLedgerSourceCode being updated") @QueryValue("id")
      id: UUID,
      @Body @Valid
      dto: GeneralLedgerSourceCodeDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerSourceCodeDTO {
      logger.info("Requested Update GeneralLedgerSourceCode {}", dto)

      val user = userService.fetchUser(authentication)
      val response = generalLedgerSourceCodeService.update(id, dto, user.myCompany())

      logger.debug("Requested Update GeneralLedgerSourceCode {} resulted in {}", dto, response)

      return response
   }

   @Delete(value = "/{id}")
   @Throws(NotFoundException::class)
   @Operation(tags = ["GeneralLedgerSourceCodeEndpoints"], summary = "Delete a single GeneralLedgerSourceCode", description = "Delete a single GeneralLedgerSourceCode", operationId = "generalLedgerSourceCode-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If SourceCode was successfully deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerSourceCode was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete account", authentication)

      val user = userService.fetchUser(authentication)

      return generalLedgerSourceCodeService.delete(id, user.myCompany())
   }
}
