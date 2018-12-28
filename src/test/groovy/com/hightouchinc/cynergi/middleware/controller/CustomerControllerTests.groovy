package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.ControllerTestsBase
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import com.hightouchinc.cynergi.middleware.exception.NotFoundException
import com.hightouchinc.cynergi.test.data.loader.CustomerTestDataLoaderService

import static io.micronaut.http.HttpRequest.GET

class CustomerControllerTests extends ControllerTestsBase {
   def customerTestDataLoaderService = applicationContext.getBean(CustomerTestDataLoaderService)

   def "fetch one customer" () {
      when:
         def customer = customerTestDataLoaderService.stream(1).findFirst().orElseThrow { new NotFoundException("Unable to create Customer") }
      then:
         client.toBlocking().retrieve(GET("/api/v1/customers/${customer.id}"), Customer) == customer
   }
}
