package com.cynergisuite.middleware.accounting.general.ledger.detail.infrastructure

import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailDTO
import com.cynergisuite.middleware.accounting.general.ledger.detail.GeneralLedgerDetailService
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
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
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/general-ledger/detail")
class GeneralLedgerDetailController @Inject constructor(
   private val generalLedgerDetailService: GeneralLedgerDetailService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerDetailController::class.java)

   @Get(uri = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerDetailEndpoints"], summary = "Fetch a single GeneralLedgerDetailDTO", description = "Fetch a single GeneralLedgerDetailDTO that is associated with the logged-in user's company", operationId = "GeneralLedgerDetail-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerDetailDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @QueryValue("id") id: Long,
      authentication: Authentication
   ): GeneralLedgerDetailDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Fetching GeneralLedgerDetail by ID {}", id)

      val response = generalLedgerDetailService.fetchOne(id, userCompany) ?: throw NotFoundException(id)

      logger.debug("Fetching GeneralLedgerDetail by {} resulted in", id, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerDetailEndpoints"], summary = "Create a GeneralLedgerDetailEntity", description = "Create an GeneralLedgerDetailEntity", operationId = "GeneralLedgerDetail-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body dto: GeneralLedgerDetailDTO,
      authentication: Authentication
   ): GeneralLedgerDetailDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Create GeneralLedgerDetail {}", dto)

      val response = generalLedgerDetailService.create(dto, userCompany)

      logger.debug("Requested Create GeneralLedgerDetail {} resulted in {}", dto, response)

      return response
   }

   @Put(uri = "/{id:[0-9]+}", processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerDetailEndpoints"], summary = "Update a GeneralLedgerDetailEntity", description = "Update an GeneralLedgerDetailEntity from a body of GeneralLedgerDetailDTO", operationId = "GeneralLedgerDetail-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update GeneralLedgerDetail", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerDetailDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerDetail was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @QueryValue("id") id: Long,
      @Body dto: GeneralLedgerDetailDTO,
      authentication: Authentication
   ): GeneralLedgerDetailDTO {
      val user = userService.findUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update GeneralLedgerDetail {}", dto)

      val response = generalLedgerDetailService.update(id, dto, userCompany)

      logger.debug("Requested Update GeneralLedgerDetail {} resulted in {}", dto, response)

      return response
   }
}
