package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import com.hightouchinc.cynergi.middleware.service.CustomerService
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class CustomerTestDataLoader {
   static Stream<Customer> stream(int number = 1) {
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def name = faker.name()
      final def numbers = faker.number()
      final def dates = faker.date()

      return IntStream.range(0, value).mapToObj {
         new Customer(
            numbers.digits(10),
            name.firstName(),
            name.lastName(),
            name.username(),
            dates.birthday(18, 85)
         )
      }
   }
}

@Singleton
@CompileStatic
class CustomerTestDataLoaderService {
   private final CustomerService customerService

   CustomerTestDataLoaderService(CustomerService customerService) {
      this.customerService = customerService
   }

   Stream<Customer> stream(int number = 1) {
      return CustomerTestDataLoader.stream(number)
         .map { customerService.save(it) }
   }
}
