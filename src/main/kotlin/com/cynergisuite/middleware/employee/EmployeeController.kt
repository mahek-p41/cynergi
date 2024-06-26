package com.cynergisuite.middleware.employee

import com.cynergisuite.domain.Page
import com.cynergisuite.common.exceptions.PageOutOfBoundsException
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Secured(IS_AUTHENTICATED)
@Controller("/api/employee")
class EmployeeController {

}
