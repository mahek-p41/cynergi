package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.CrudControllerBase
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.service.CompanyService
import io.micronaut.http.annotation.Controller

@Controller("/api/v1/companies")
class CompanyController(
   companyService: CompanyService
): CrudControllerBase<Company>(
   identityService = companyService
)
