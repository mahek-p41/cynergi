package com.cynergisuite.middleware.accounting.general.ledger.control.infrastructure

import com.cynergisuite.middleware.accounting.general.ledger.control.GeneralLedgerControlDTO
import com.cynergisuite.middleware.accounting.general.ledger.control.GeneralLedgerControlService
import com.cynergisuite.middleware.authentication.infrastructure.AreaControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.ValidationException
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@AreaControl("GL")
@Controller("/api/general-ledger/control")
class GeneralLedgerControlController @Inject constructor(
   private val generalLedgerControlService: GeneralLedgerControlService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(GeneralLedgerControlController::class.java)

   @Get(produces = [APPLICATION_JSON])
   @Operation(tags = ["GeneralLedgerControlEndpoints"], summary = "Fetch a single GeneralLedgerControlDTO", description = "Fetch a single GeneralLedgerControlDTO that is associated with the logged-in user's company", operationId = "GeneralLedgerControl-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerControlDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerControl was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      authentication: Authentication
   ): GeneralLedgerControlDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Fetching GeneralLedgerControl by {}", userCompany)

      val response = generalLedgerControlService.fetchOne(userCompany) ?: throw NotFoundException("General ledger control record of the company")

      logger.debug("Fetching GeneralLedgerControl by {} resulted in", userCompany, response)

      return response
   }

   @Post(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerControlEndpoints"], summary = "Create a GeneralLedgerControlEntity", description = "Create an GeneralLedgerControlEntity", operationId = "GeneralLedgerControl-create")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerControlDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun create(
      @Body @Valid
      dto: GeneralLedgerControlDTO,
      authentication: Authentication
   ): GeneralLedgerControlDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Create GeneralLedgerControl {}", dto)

      val response = generalLedgerControlService.create(dto, userCompany)

      logger.debug("Requested Create GeneralLedgerControl {} resulted in {}", dto, response)

      return response
   }

   @Put(processes = [APPLICATION_JSON])
   @Throws(ValidationException::class, NotFoundException::class)
   @Operation(tags = ["GeneralLedgerControlEndpoints"], summary = "Update a GeneralLedgerControlEntity", description = "Update an GeneralLedgerControlEntity from a body of GeneralLedgerControlDTO", operationId = "GeneralLedgerControl-update")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", description = "If successfully able to update GeneralLedgerControl", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = GeneralLedgerControlDTO::class))]),
         ApiResponse(responseCode = "400", description = "If one of the required properties in the payload is missing"),
         ApiResponse(responseCode = "404", description = "The requested GeneralLedgerControl was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun update(
      @Body @Valid
      dto: GeneralLedgerControlDTO,
      authentication: Authentication
   ): GeneralLedgerControlDTO {
      val user = userService.fetchUser(authentication)
      val userCompany = user.myCompany()
      logger.info("Requested Update GeneralLedgerControl {}", dto)

      val response = generalLedgerControlService.update(dto, userCompany)

      logger.debug("Requested Update GeneralLedgerControl {} resulted in {}", dto, response)

      return response
   }
}
