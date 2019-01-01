package com.hightouchinc.cynergi.middleware.controller

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.controller.spi.ControllerTestsBase
import com.hightouchinc.cynergi.middleware.domain.Page
import com.hightouchinc.cynergi.middleware.entity.Customer
import com.hightouchinc.cynergi.middleware.service.CustomerService
import com.hightouchinc.cynergi.test.data.loader.CustomerTestDataLoaderService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.hateos.JsonError

import static io.micronaut.http.HttpRequest.GET
import static io.micronaut.http.HttpStatus.NOT_FOUND

class CustomerControllerTests extends ControllerTestsBase {
   final def url = "/api/v1/customers"
   def customerTestDataLoaderService = applicationContext.getBean(CustomerTestDataLoaderService)
   def customerService = applicationContext.getBean(CustomerService)

   def "fetch one customer"() {
      when:
      final def customer = customerTestDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Customer")}

      then:
      client.retrieve(GET("$url/${customer.id}"), Customer) == customer
   }

   def "fetch one customer not found"() {
      when:
      client.exchange(GET("$url/0"))

      then:
      final HttpClientResponseException exception = thrown(HttpClientResponseException)
      exception.response.status == NOT_FOUND
      exception.response.getBody(JsonError).orElse(null)?.message == "Resource 0 was unable to be found"
   }

   def "search for customer John" () {
      given:
      final def faker = new Faker()
      final def number = faker.number()
      final def birthDateFaker = faker.date()
      final def customers = customerService.save([
         new Customer(
            number.digits(10),
            "Johnny",
            "Begood",
            "J",
            birthDateFaker.birthday(18, 55)
         ),
         new Customer(
            number.digits(10),
            "Homer",
            "Simpson",
            "The Big H",
            birthDateFaker.birthday(18, 55)
         ),
         new Customer(
            number.digits(10),
            "Johnathan",
            "Simpson",
            "The Big J",
            birthDateFaker.birthday(18, 55)
         )
      ])

      when:
      def johns = client.retrieve(GET("$url/search/john"), Page)
      def johnSimpsons = client.retrieve(GET("$url/search/john%20simpson"), Page)

      then:
      johns.content.size() == 2

      johns.content[0].id == customers[0].id
      johns.content[0].firstName == customers[0].firstName

      johns.content[1].id == customers[2].id
      johns.content[1].firstName == customers[2].firstName

      johnSimpsons.content.size() == 1
   }
}
