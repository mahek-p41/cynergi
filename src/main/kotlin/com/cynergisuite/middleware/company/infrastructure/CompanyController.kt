package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.company.CompanyService
import com.cynergisuite.middleware.company.CompanyValueObject
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
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

   @Get(uri = "/{id}", produces = [MediaType.APPLICATION_JSON])
   @Throws(NotFoundException::class)
   @Operation(tags = ["CompanyEndpoints"], summary = "Fetch a single company", description = "Fetch a single company by ID", operationId = "company-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = CompanyValueObject::class))]),
         ApiResponse(responseCode = "404", description = "The requested company was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: Long,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): CompanyValueObject {
      logger.info("Fetching company by ID {}", id)

      val response = companyService.fetchById(id) ?: throw NotFoundException(id)

      logger.debug("Fetching company by {} resulted in", id, response)

      return response
   }

   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Throws(PageOutOfBoundsException::class)
   @Operation(tags = ["CompanyEndpoints"], summary = "Fetch a listing of companies", description = "Fetch a paginated listing of companies", operationId = "company-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest")
      pageRequestIn: StandardPageRequest
   ): Page<CompanyValueObject> {
      logger.info("Fetching all companies {}", pageRequestIn)

      val pageRequest = StandardPageRequest(pageRequestIn)
      val page = companyService.fetchAll(pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      return page
   }

   @Post(processes = [APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["CompanyEndpoints"], summary = "Create a single company", description = "Create a single company.", operationId = "company-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save Company", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = CompanyValueObject::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun save(
      @Body companyVO: CompanyValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): CompanyValueObject {
      logger.info("Requested Save Company {}", companyVO)

      val response = companyService.create(companyVO)

      logger.debug("Requested Save Company {} resulted in {}", companyVO, response)

      return response
   }

   @Put(uri = "/{id}", processes = [APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["CompanyEndpoints"], summary = "Create a single company", description = "Create a single company.", operationId = "company-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Company", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = CompanyValueObject::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested Company was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: Long,
      @Body companyVO: CompanyValueObject,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): CompanyValueObject {
      logger.info("Requested Update Company {}", companyVO)

      val response = companyService.update(id, companyVO)

      logger.debug("Requested Update Company {} resulted in {}", companyVO, response)

      return response
   }
}
