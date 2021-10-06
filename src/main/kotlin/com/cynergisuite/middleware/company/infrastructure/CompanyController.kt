package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.company.CompanyDTO
import com.cynergisuite.middleware.company.CompanyService
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS
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

@Secured(IS_ANONYMOUS)
@Controller("/api/company")
class CompanyController @Inject constructor(
   private val companyService: CompanyService
) {
   private val logger: Logger = LoggerFactory.getLogger(CompanyController::class.java)

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["CompanyEndpoints"], summary = "Fetch a listing of companies", description = "Fetch a paginated listing of companies", operationId = "company-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest")
      pageRequestIn: StandardPageRequest
   ): Page<CompanyDTO> {
      logger.info("Fetching all companies {}", pageRequestIn)

      val pageRequest = StandardPageRequest(pageRequestIn)
      val page = companyService.fetchAll(pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      return page
   }
}
