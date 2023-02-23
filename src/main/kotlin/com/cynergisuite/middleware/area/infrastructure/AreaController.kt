package com.cynergisuite.middleware.area.infrastructure

import com.cynergisuite.middleware.area.AreaService
import com.cynergisuite.middleware.area.findAreaType
import com.cynergisuite.middleware.authentication.user.UserService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(IS_AUTHENTICATED)
@Controller("/api/area")
class AreaController @Inject constructor(
   private val areaService: AreaService,
   private val userService: UserService,
) {
   private val logger: Logger = LoggerFactory.getLogger(AreaController::class.java)

   @Get("/available/{area}")
   @Operation(tags = ["AreaEndpoints"], description = "Check if an area is available. OK if it is Not Found if it isn't", operationId = "area-fetchEnabled")
   fun areaEnabled(
      @QueryValue("area") area: String,
      authentication: Authentication,
   ): HttpResponse<*> {
      val user = userService.fetchUser(authentication)
      val maybeArea = findAreaType(area)

      logger.debug("Checking if {} is enabled for {}", area, user.myCompany().id)
      logger.trace("Maybe area: {}", maybeArea)

      return if (areaService.isEnabledFor(user.myCompany(), maybeArea)) {
         logger.debug("{} was enabled for {}", area, user.myCompany().id)

         HttpResponse.ok<Any>()
      } else {
         logger.debug("{} was not enabled for {}", area, user.myCompany().id)

         HttpResponse.notFound<Any>()
      }
   }
}
