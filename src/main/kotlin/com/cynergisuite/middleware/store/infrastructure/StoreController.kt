package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.store.StoreService
import com.cynergisuite.middleware.store.StoreValueObject
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
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
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Controller("/api/store")
class StoreController @Inject constructor(
   private val storeService: StoreService
) {
   private val logger: Logger = LoggerFactory.getLogger(StoreController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("store-fetchOne")
   @Get(value = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["StoreEndpoints"], summary = "Fetch a single Store", description = "Fetch a single Store by it's system generated primary key", operationId = "audit-fetchOne")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = StoreValueObject::class))]),
      ApiResponse(responseCode = "404", description = "The requested Store was unable to be found"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Store with", `in` = PATH) @QueryValue("id") id: Long
   ): StoreValueObject {
      logger.info("Fetching Store by {}", id)

      val response = storeService.fetchById(id = id) ?: throw NotFoundException(id)

      logger.debug("Fetching Store by {} resulted in {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("store-fetchAll")
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["StoreEndpoints"], summary = "Fetch a listing of Stores", description = "Fetch a paginated listing of Stores", operationId = "store-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest") pageRequest: PageRequest
   ): Page<StoreValueObject> {
      logger.info("Fetching all stores {}", pageRequest)

      val page = storeService.fetchAll(pageRequest)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      return page
   }
}
