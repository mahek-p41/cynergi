package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.CrudControllerBase
import com.hightouchinc.cynergi.middleware.domain.Page
import com.hightouchinc.cynergi.middleware.entity.Customer
import com.hightouchinc.cynergi.middleware.entity.CustomerDto
import com.hightouchinc.cynergi.middleware.service.CustomerCrudService
import com.hightouchinc.cynergi.middleware.validator.CustomerValidator
import io.micronaut.context.annotation.Parameter
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/api/v1/customers")
class CustomerController(
   private val customerService: CustomerCrudService,
   customerValidator: CustomerValidator
): CrudControllerBase<CustomerDto>(
   crudService = customerService,
   validator = customerValidator
) {
   @Get("/search/{searchString}", produces = [APPLICATION_JSON])
   fun search(
      @Parameter("searchString") searchString: String
   ): Page<Customer> {
      return customerService.searchForCustomers(customerSearchString = searchString)
   }
}
