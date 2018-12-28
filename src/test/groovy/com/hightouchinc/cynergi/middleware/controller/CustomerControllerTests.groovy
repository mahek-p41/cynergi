package com.hightouchinc.cynergi.middleware.controller

import com.hightouchinc.cynergi.middleware.controller.spi.ControllerTestsBase
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import com.hightouchinc.cynergi.middleware.exception.NotFound
import com.hightouchinc.cynergi.test.data.loader.CustomerTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpStatus.NOT_FOUND

class CustomerControllerTests extends ControllerTestsBase {
   final def url = "/api/v1/customers"
   def customerTestDataLoaderService = applicationContext.getBean(CustomerTestDataLoaderService)

   def "fetch one customer"() {
      when:
      def customer = customerTestDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Customer")}

      then:
      client.toBlocking().retrieve(GET("$url/${customer.id}"), Customer) == customer
   }

   def "fetch one customer not found"() {
      when:
      client.toBlocking().exchange(GET("$url/0"))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(NotFound.class).orElse(null) == new NotFound("0")
   }
}
