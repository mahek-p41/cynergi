package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.CrudControllerBase
import com.hightouchinc.cynergi.middleware.entity.CompanyDto
import com.hightouchinc.cynergi.middleware.service.CompanyService
import com.hightouchinc.cynergi.middleware.validator.CompanyValidator
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.validation.Validated
import javax.validation.Valid

@Validated
@Controller("/api/v1/companies")
class CompanyController(
   private val companyService: CompanyService,
   private val companyValidator: CompanyValidator
): CrudControllerBase<CompanyDto>(
   crudService = companyService
) {

   @Post(processes = [MediaType.APPLICATION_JSON])
   override fun save(
      @Valid @Body dto: CompanyDto
   ): CompanyDto {
      companyValidator.validateSave(dto = dto)

      return companyService.create(dto = dto)
   }

   @Put(processes = [MediaType.APPLICATION_JSON])
   override fun update(
      @Valid @Body dto: CompanyDto
   ): CompanyDto {
      companyValidator.validateUpdate(dto = dto)

      return companyService.update(dto = dto)
   }
}
