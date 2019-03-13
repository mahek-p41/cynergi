package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.authentication.AuthenticationService
import com.hightouchinc.cynergi.middleware.entity.AreaDto
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.AreaService
import com.hightouchinc.cynergi.middleware.validator.AreaValidator
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

@Secured(IS_AUTHENTICATED)
@Validated
@Controller("/api/areas")
class AreaController @Inject constructor(
   private val areaService: AreaService,
   private val areaValidator: AreaValidator,
   private val authenticationService: AuthenticationService
) {
   private val logger: Logger = LoggerFactory.getLogger(AreaController::class.java)

   @Throws(NotFoundException::class)
   @Get(produces = [APPLICATION_JSON])
   fun fetch(
      authentication: Authentication
   ): List<AreaDto> {
      logger.debug("Fetching areas for {}", authentication.name)

      val cynergiUser = authenticationService.decodeUserDetails(authentication = authentication)

      areaValidator.validateUser(cynergiUser = cynergiUser)

      return areaService.findAreasByLevelAndCompany(level = cynergiUser.level, companyId = cynergiUser.companyId)
   }
}
