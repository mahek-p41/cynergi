package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.CrudControllerBase
import com.hightouchinc.cynergi.middleware.entity.CompanyDto
import com.hightouchinc.cynergi.middleware.service.CompanyCrudService
import com.hightouchinc.cynergi.middleware.validator.CompanyValidator
import io.micronaut.http.annotation.Controller

@Controller("/api/v1/companies")
class CompanyController(
   companyService: CompanyCrudService,
   companyValidator: CompanyValidator
): CrudControllerBase<CompanyDto>(
   crudService = companyService,
   validator = companyValidator
)
