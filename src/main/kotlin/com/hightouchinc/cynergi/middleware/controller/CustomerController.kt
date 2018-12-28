package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.CrudControllerBase
import com.hightouchinc.cynergi.middleware.data.domain.Page
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import com.hightouchinc.cynergi.middleware.service.CustomerService
import io.micronaut.context.annotation.Parameter
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/api/v1/customers")
class CustomerController(
   private val customerService: CustomerService
): CrudControllerBase<Customer>(
   identityService = customerService
) {
   @Get("/search/{searchString}")
   fun search(
      @Parameter("searchString") searchString: String
   ): Page<Customer> {
      return customerService.searchForCustomers(customerSearchString = searchString)
   }
}
