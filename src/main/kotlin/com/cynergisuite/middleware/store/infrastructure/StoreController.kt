package com.cynergisuite.middleware.store.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.SearchPageRequest
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.authentication.infrastructure.AccessControl
import com.cynergisuite.middleware.authentication.user.UserService
import com.cynergisuite.middleware.error.NotFoundException
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.store.StoreDTO
import com.cynergisuite.middleware.store.StoreService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.ParameterIn.PATH
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Secured(IS_AUTHENTICATED)
@Controller("/api/store")
class StoreController @Inject constructor(
   private val storeService: StoreService,
   private val userService: UserService
) {
   private val logger: Logger = LoggerFactory.getLogger(StoreController::class.java)

   @Throws(NotFoundException::class)
   @AccessControl("store-fetchOne", StoreAccessControlProvider::class)
   @Get(value = "/{id:[0-9]+}", produces = [APPLICATION_JSON])
   @Operation(tags = ["StoreEndpoints"], summary = "Fetch a single Store", description = "Fetch a single Store by it's system generated primary key", operationId = "store-fetchOne")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = StoreDTO::class))]),
         ApiResponse(responseCode = "404", description = "The requested Store was unable to be found"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchOne(
      @Parameter(description = "Primary Key to lookup the Store with", `in` = PATH) @QueryValue("id")
      id: Long,
      authentication: Authentication
   ): StoreDTO {
      logger.info("Fetching Store by id {}", id)

      val user = userService.fetchUser(authentication)
      val response = storeService.fetchById(id, user.myCompany()) ?: throw NotFoundException(id)

      logger.debug("Fetching Store by {} resulted in {}", id, response)

      return response
   }

   @Throws(PageOutOfBoundsException::class)
   @AccessControl("store-fetchAll", StoreAccessControlProvider::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["StoreEndpoints"], summary = "Fetch a listing of Stores", description = "Fetch a paginated listing of Stores", operationId = "store-fetchAll")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The the result is empty"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = QUERY, required = false) @QueryValue("pageRequest")
      pageRequest: StandardPageRequest,
      authentication: Authentication
   ): Page<StoreDTO> {
      logger.info("Fetching all stores {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = storeService.fetchAll(pageRequest, user)

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest)
      }

      return page
   }

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "/search{?pageRequest*}", produces = [MediaType.APPLICATION_JSON])
   @Operation(tags = ["AccountEndpoints"], summary = "Search for a list of stores", description = "search of a paginated listing of stores based on a query", operationId = "store-search")
   @ApiResponses(
      value = [
         ApiResponse(responseCode = "200", content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
         ApiResponse(responseCode = "204", description = "The requested Store was unable to be found, or the result is empty"),
         ApiResponse(responseCode = "401", description = "If the user calling this endpoint does not have permission to operate it"),
         ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
      ]
   )
   fun search(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest")
      @Valid
      pageRequest: SearchPageRequest,
      authentication: Authentication,
      httpRequest: HttpRequest<*>
   ): Page<StoreDTO> {
      logger.info("Search for stores {}", pageRequest)

      val user = userService.fetchUser(authentication)
      val page = storeService.search(user.myCompany(), pageRequest, httpRequest.findLocaleWithDefault())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }
}
