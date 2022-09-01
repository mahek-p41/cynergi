package com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure

import com.cynergisuite.domain.GeneralLedgerSearchReportFilterRequest
import com.cynergisuite.domain.GeneralLedgerSourceReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerAccountPostingDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerAccountPostingResponseDTO
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSearchReportTemplate
import com.cynergisuite.middleware.accounting.general.ledger.GeneralLedgerSourceReportTemplate
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
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
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
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
@Controller("/api/general-ledger/detail")
class GeneralLedgerDetailController @Inject constructor(
   private val generalLedgerDetailService: GeneralLedgerDetailService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerDetailController::class.java)

   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerDetailEndpoints"], summary = "Fetch a single GeneralLedgerDetailDTO", description = "Fetch a single GeneralLedgerDetailDTO that is associated with the logged-in user's company", operationId = "generalLedgerDetail-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerDetailDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication
   ): GeneralLedgerDetailDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Fetching GeneralLedgerDetail by ID {}", id)

      val response = generalLedgerDetailService.fetchOne(id, userCompany) ?: throw NotFoundException(id)

      logger.debug("Fetching GeneralLedgerDetail by {} resulted in", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerDetailEndpoints"], summary = "Fetch a listing of General Ledger Details", description = "Fetch a paginated listing of General Ledger Details", operationId = "generalLedgerDetail-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Detail was unable to be found, or the result is empty"),
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
   ): Page<GeneralLedgerDetailDTO> {
      logger.info("Fetching all General Ledger Details {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = generalLedgerDetailService.fetchAll(user.myCompany(), pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerDetailEndpoints"], summary = "Create a GeneralLedgerDetailEntity", description = "Create an GeneralLedgerDetailEntity", operationId = "generalLedgerDetail-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerDetailDTO,
      authentication: Authentication
   ): GeneralLedgerDetailDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Create GeneralLedgerDetail {}", dto)

      val response = generalLedgerDetailService.create(dto, userCompany)

      logger.debug("Requested Create GeneralLedgerDetail {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerDetailEndpoints"], summary = "Update a GeneralLedgerDetailEntity", description = "Update an GeneralLedgerDetailEntity from a body of GeneralLedgerDetailDTO", operationId = "generalLedgerDetail-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update GeneralLedgerDetail", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: UUID,
      @Body @Valid
      dto: GeneralLedgerDetailDTO,
      authentication: Authentication
   ): GeneralLedgerDetailDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update GeneralLedgerDetail {}", dto)

      val response = generalLedgerDetailService.update(id, dto, userCompany)

      logger.debug("Requested Update GeneralLedgerDetail {} resulted in {}", dto, response)

      return response
   }

   @Get(uri = "/search-report{?filterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerDetailEndpoints"], summary = "Fetch a General Ledger Search Report", description = "Fetch a General Ledger Search Report", operationId = "generalLedgerJournal-fetchReport")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerSearchReportFilterRequest::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Search Report was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchReport(
      @Parameter(name = "filterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("filterRequest")
      filterRequest: GeneralLedgerSearchReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerSearchReportTemplate {
      logger.info("Fetching all General Ledger Search reports{}")

      val user = userService.fetchUser(authentication)
      return generalLedgerDetailService.fetchReport(user.myCompany(), filterRequest)
   }

   @Get(uri = "/source-report{?sourceReportFilterRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerJournalEndpoints"], summary = "Fetch a General Ledger Source Report", description = "Fetch a General Ledger Source Report", operationId = "generalLedgerJournal-fetchSourceReport")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerSourceReportFilterRequest::class))]),
         ApiResponse(responseCode = "204", description = "The requested General Ledger Source Report was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchSourceReport(
      @Parameter(name = "sourceReportFilterRequest", `in` = QUERY, required = false)
      @Valid @QueryValue("sourceReportFilterRequest")
      sourceReportFilterRequest: GeneralLedgerSourceReportFilterRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerSourceReportTemplate {
      logger.info("Fetching General Ledger Details for the General Ledger Source Report {}")

      val user = userService.fetchUser(authentication)
      return generalLedgerDetailService.fetchSourceReport(user.myCompany(), sourceReportFilterRequest)
   }


   @Post(uri = "/subroutine", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerDetailEndpoints"], summary = "Post an accounting entry to the GL ", description = "Post an accounting entry to the GL", operationId = "generalLedgerDetail-subroutine")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerAccountPostingResponseDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun subroutine(
      @Body @Valid
      dto: GeneralLedgerAccountPostingDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): GeneralLedgerAccountPostingResponseDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Posting accounting entry to the General Ledger {}", dto)

      val response = generalLedgerDetailService.postEntry(dto, userCompany, httpRequest.findLocaleWithDefault())

      logger.debug("Posting GeneralLedgerDetail {} resulted in {}", dto, response)

      return response
   }
}
