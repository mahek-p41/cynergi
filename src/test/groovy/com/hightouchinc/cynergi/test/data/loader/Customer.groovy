package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.github.javafaker.Name
import com.hightouchinc.cynergi.middleware.data.transfer.Customer
import com.hightouchinc.cynergi.middleware.service.CustomerService
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.ZoneId
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class CustomerTestDataLoader {
   static Stream<Customer> stream(int number = 1) {
      final int value = number > 0 ? number : 1
      final Faker faker = new Faker()
      final Name name = faker.name()
      final def numbers = faker.number()
      final def dates = faker.date()
      final def bool = faker.bool()

      return IntStream.range(0, value).mapToObj {
         new Customer(
            numbers.digits(10),
            name.firstName(),
            name.lastName(),
            name.username(),
            dates.birthday(18, 85).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            numbers.digits(15),
            bool.bool(),
            bool.bool(),
            bool.bool()
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
