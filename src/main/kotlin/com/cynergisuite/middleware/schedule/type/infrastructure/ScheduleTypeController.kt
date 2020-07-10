package com.cynergisuite.middleware.schedule.type.infrastructure

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.StandardPageRequest
import com.cynergisuite.extensions.findLocaleWithDefault
import com.cynergisuite.middleware.error.PageOutOfBoundsException
import com.cynergisuite.middleware.schedule.type.ScheduleTypeService
import com.cynergisuite.middleware.schedule.type.ScheduleTypeValueObject
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
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

@Secured(IS_AUTHENTICATED)
@Controller("/api/schedule/type")
class ScheduleTypeController @Inject constructor(
   private val scheduleTypeService: ScheduleTypeService
) {
   private val logger: Logger = LoggerFactory.getLogger(ScheduleTypeController::class.java)

   @Throws(PageOutOfBoundsException::class)
   @Get(uri = "{?pageRequest*}", produces = [APPLICATION_JSON])
   @Operation(tags = ["ScheduleTypeEndpoints"], summary = "Fetch a listing of Audits", description = "Fetch a paginated listing of Audits", operationId = "audit-fetchAll")
   @ApiResponses(value = [
      ApiResponse(responseCode = "200", description = "If there are Audits that can be loaded within the bounds of the provided page", content = [Content(mediaType = APPLICATION_JSON, schema = Schema(implementation = Page::class))]),
      ApiResponse(responseCode = "204", description = "The requested Audit was unable to be found, or the result is empty"),
      ApiResponse(responseCode = "500", description = "If an error occurs within the server that cannot be handled")
   ])
   fun fetchAll(
      @Parameter(name = "pageRequest", `in` = ParameterIn.QUERY, required = false) @QueryValue("pageRequest") pageRequest: StandardPageRequest,
      httpRequest: HttpRequest<*>
   ): Page<ScheduleTypeValueObject> {
      logger.info("Fetching all schedule types {} {}", pageRequest)

      val page =  scheduleTypeService.fetchAll(pageRequest, httpRequest.findLocaleWithDefault())

      if (page.elements.isEmpty()) {
         throw PageOutOfBoundsException(pageRequest = pageRequest)
      }

      return page
   }
}
