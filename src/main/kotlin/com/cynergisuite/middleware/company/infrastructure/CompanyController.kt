package com.cynergisuite.middleware.company.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.company.CompanyDTO
import com.cynergisuite.middleware.company.CompanyService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.*
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
import java.util.UUID
import javax.inject.Inject
import javax.validation.Valid

@Secured(IS_ANONYMOUS)
@Controller("/api/company")
class CompanyController @Inject constructor(
   private val companyService: CompanyService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(CompanyController::class.java)

   @Get(uri = "/{id}", produces = [APPLICATION_JSON])
   @Throws(NotFoundException::class)
   @Operation(tags = ["CompanyEndpoints"], summary = "Fetch a single company", description = "Fetch a single company by ID", operationId = "company-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = CompanyDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested company was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: UUID,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): CompanyDTO {
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
   ): Page<CompanyDTO> {
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
         ApiResponse(responseCode = "200", description = "If successfully able to save Company", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = CompanyDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun save(
      @Body @Valid
      companyDTO: CompanyDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): CompanyDTO {
      logger.info("Requested Save Company {}", companyDTO)

      val response = companyService.create(companyDTO)

      logger.debug("Requested Save Company {} resulted in {}", companyDTO, response)

      return response
   }

   @Put(uri = "/{id}", processes = [APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["CompanyEndpoints"], summary = "Create a single company", description = "Create a single company.", operationId = "company-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Company", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = CompanyDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested Company was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: UUID,
      @Body @Valid
      companyDTO: CompanyDTO,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): CompanyDTO {
      logger.info("Requested Update Company {}", companyDTO)

      val response = companyService.update(id, companyDTO)

      logger.debug("Requested Update Company {} resulted in {}", companyDTO, response)

      return response
   }

   @Delete(uri = "/{id}", processes = [APPLICATION_JSON])
   @AccessControl
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["CompanyEndpoints"], summary = "Delete a company", description = "Delete a single company", operationId = "company-delete")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the company was able to be deleted"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun delete(
      @QueryValue("id") id: UUID,
      httpRequest: HttpRequest<*>,
      authentication: Authentication
   ) {
      logger.debug("User {} requested delete company", authentication)

      val user = userService.fetchUser(authentication)

      return companyService.delete(id)
   }
}
