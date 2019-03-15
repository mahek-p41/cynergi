package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.authentication.AuthenticationService
import com.hightouchinc.cynergi.middleware.dto.MenuTreeDto
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.MenuService
import com.hightouchinc.cynergi.middleware.validator.MenuValidator
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Operation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(IS_AUTHENTICATED)
@Validated
@Controller("/api/menus")
class MenuController(
   private val authenticationService: AuthenticationService,
   private val menuService: MenuService,
   private val menuValidator: MenuValidator
) {
   private val logger: Logger = LoggerFactory.getLogger(MenuController::class.java)

   @Operation(
      method = "GET",
      operationId = "fetchMenus",
      summary = "Loads menus and modules",
      description = "Loads the menus and modules that are accessible by the authenticated user"
   )
   @Throws(NotFoundException::class)
   @Get(produces = [MediaType.APPLICATION_JSON])
   fun fetch(
      authentication: Authentication
   ): Set<MenuTreeDto> {
      val cynergiUser = authenticationService.decodeUserDetails(authentication)

      logger.debug("Menus requested by {}", cynergiUser)

      menuValidator.validateUser(cynergiUser)

      return menuService.findAllBy(level = cynergiUser.level, companyId = cynergiUser.companyId)
   }
}
