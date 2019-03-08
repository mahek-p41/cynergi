package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.entity.AreaDto
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.AreaService
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
@Controller("/api/TODO add valid path here")
class AreaController @Inject constructor(
   private val areaService: AreaService
) {
   private val logger: Logger = LoggerFactory.getLogger(AreaController::class.java)

   @Throws(NotFoundException::class)
   @Get(produces = [APPLICATION_JSON])
   fun fetch(
      authentication: Authentication
   ): AreaDto {
      authentication.
      val response: AreaDto? = areaService

      return response
   }
}
