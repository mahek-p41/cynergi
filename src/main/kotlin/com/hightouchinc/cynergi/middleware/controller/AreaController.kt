package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.entity.AreaDto
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.exception.UnauthorizedException
import com.hightouchinc.cynergi.middleware.service.AreaService
import io.micronaut.context.annotation.Value
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.micronaut.validation.Validated
import org.apache.commons.lang3.math.NumberUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Validated
@Controller("/api/TODO add valid path here")
class AreaController @Inject constructor(
   private val areaService: AreaService,
   @Value("\${cynergi.security.roles-level-prefix}") private val rolesLevelPrefix: String
) {
   private val logger: Logger = LoggerFactory.getLogger(AreaController::class.java)

   @Throws(NotFoundException::class)
   @Get(produces = [APPLICATION_JSON])
   fun fetch(
      authentication: Authentication
   ): AreaDto {
      val roles = authentication.attributes["roles"] as Collection<*>
      val level: Int = roles.asSequence()
         .filter { it is String }
         .map { it as String }
         .filter { it.startsWith(rolesLevelPrefix) }
         .map { it.substringAfter(rolesLevelPrefix) }
         .filter { NumberUtils.isDigits(it) }
         .map { it.toInt() }
         .firstOrNull() ?: throw UnauthorizedException() // TODO handle in ErrorHandler, add message about no levels been assigned yet via the UnauthrorizedException constructor

      return areaService.findAreasByLevel(level = level) ?: throw UnauthorizedException() // TODO add localizized message about no areas assigned based on current level
   }
}
