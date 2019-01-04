package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.CrudControllerBase
import com.hightouchinc.cynergi.middleware.domain.Page
import com.hightouchinc.cynergi.middleware.entity.Customer
import com.hightouchinc.cynergi.middleware.entity.CustomerDto
import com.hightouchinc.cynergi.middleware.service.CustomerService
import com.hightouchinc.cynergi.middleware.validator.CustomerValidator
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import javax.validation.Valid

@Controller("/api/v1/customers")
class CustomerController(
   private val customerService: CustomerService,
   private val customerValidator: CustomerValidator
): CrudControllerBase<CustomerDto>(
   crudService = customerService
) {

   @Get("/search/{searchString}", produces = [APPLICATION_JSON])
   fun search(
      @QueryValue("searchString") searchString: String
   ): Page<Customer> {
      return customerService.searchForCustomers(customerSearchString = searchString)
   }

   @Post(processes = [APPLICATION_JSON])
   override fun save(
      @Valid @Body dto: CustomerDto
   ): CustomerDto {
      customerValidator.validateSave(dto = dto)

      return customerService.save(dto = dto)
   }

   @Put(processes = [APPLICATION_JSON])
   override fun update(
      @Valid @Body dto: CustomerDto
   ): CustomerDto {
      customerValidator.validateUpdate(dto = dto)

      return customerService.update(dto = dto)
   }
}
