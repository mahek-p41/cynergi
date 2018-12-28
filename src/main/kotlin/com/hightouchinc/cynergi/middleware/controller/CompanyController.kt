package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.data.transfer.Company
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.middleware.service.CompanyService
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/api/v1/companies")
class CompanyController(
   companyService: CompanyService
): CrudControllerBase<Company>(
   identityService = companyService
)
