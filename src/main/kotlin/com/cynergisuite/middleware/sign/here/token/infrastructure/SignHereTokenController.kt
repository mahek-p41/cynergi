package com.cynergisuite.middleware.sign.here.token.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.company.CompanyService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import com.cynergisuite.middleware.sign.here.token.SignHereTokenDTO
import com.cynergisuite.middleware.sign.here.token.SignHereTokenService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
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
import java.util.UUID
import javax.validation.Valid

@Secured(IS_ANONYMOUS)
@Controller("/sign/here/token")
class SignHereTokenController(
   private val companyService: CompanyService,
   private val signHereTokenService: SignHereTokenService
) {
   private val logger: Logger = LoggerFactory.getLogger(SignHereTokenController::class.java)

   @Throws(NotFoundException::class)
   @Get(uri = "/{id:[0-9a-fA-F\\-]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["SignHereTokenEndpoints"], summary = "Fetch a single Agreement Signing record", description = "Fetch a single Agreement Signing record by it's system generated primary key", operationId = "signHereToken-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Agreement Signing record was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Agreement Signing record", `in` = ParameterIn.PATH) @QueryValue("id")
      id: UUID,
      companyId: UUID,
      httpRequest: HttpRequest<*>
   ): SignHereTokenDTO {
      logger.info("Fetching Agreement Signing record by {}", id)

      val company = companyService.fetchOne(companyId)
      val response = signHereTokenService.fetchById(id = id, company = company!!) ?: throw NotFoundException(id)

      logger.debug("Fetching Agreement Signing record by {} resulted in {}", id, response)

      return response
   }

   @Throws(NotFoundException::class)
   @Get("/store/{storeNumber:[0-9]+}/dataset/{dataset}", produces = [APPLICATION_JSON])
   @Operation(
      tags = ["SignHereTokenEndpoints"],
      summary = "Fetch a store's AWS token",
      description = "Fetch a store's AWS token by its store number",
      operationId = "signHereToken-fetchOneByStoreNumber"
   )
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If the token was able to be found", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = SignHereTokenDTO::class))]),
         ApiResponse(responseCode = "204", description = "The requested token was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOneByStoreNumber(
      @Parameter(name = "storeNumber", description = "Store Number to lookup the token with", `in` = ParameterIn.PATH) @QueryValue("storeNumber")
      storeNumber: Int,
      @Parameter(name = "dataset", description = "Dataset associated with the transaction", `in` = ParameterIn.PATH) @QueryValue("dataset")
      dataset: String,
      httpRequest: HttpRequest<*>
   ): SignHereTokenDTO {
      logger.info("Fetching token by store number {}", storeNumber)

      val company = companyService.fetchByDatasetCodeForEntity(dataset)
      val response = signHereTokenService.fetchByStoreNumber(storeNumber, company = company!!) ?: throw NotFoundException("$dataset -> $storeNumber")

      logger.debug("Fetch token by store {} resulted {}", storeNumber, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["SignHereTokenEndpoints"], summary = "Create a single Agreement Signing record", description = "Create a single Agreement Signing record", operationId = "signHereToken-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to save Agreement Signing record", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = SignHereTokenDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Agreement Signing record was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: SignHereTokenDTO,
      httpRequest: HttpRequest<*>
   ): SignHereTokenDTO {
      logger.info("Requested Create Agreement Signing record {}", dto)

      val company = companyService.fetchOne(dto.company!!.id!!)

      val response = signHereTokenService.create(dto = dto, company = company!!)

      logger.debug("Requested Create Agreement Signing record {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9a-fA-F\\-]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["SignHereTokenEndpoints"], summary = "Update a single Agreement Signing record", description = "Update a single Agreement Signing record", operationId = "signHereToken-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update Agreement Signing record", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = SignHereTokenDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "404", description = "The requested Agreement Signing record was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      id: UUID,
      @Body @Valid
      dto: SignHereTokenDTO,
      httpRequest: HttpRequest<*>
   ): SignHereTokenDTO {
      logger.info("Requested Audit status change or note  {}", dto)

      val company = companyService.fetchOne(dto.company!!.id!!)

      val response = signHereTokenService.update(dto, company = company!!)

      logger.debug("Requested Update Audit {} resulted in {}", dto, response)

      return response
   }
}
