package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.data.transfer.Company
import com.hightouchinc.cynergi.middleware.service.CompanyService

import javax.inject.Inject
import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

class CompanyTestDataLoader {
   static Stream<Company> stream(int number = 1) {
      final int value = number > 0 ? number : 1
      final companyFaker = new Faker().company()

      return IntStream.of(value).mapToObj {
         new Company(companyFaker.name())
      }
   }
}

@Singleton
class CompanyTestDataLoaderService {
   private final CompanyService companyService

   @Inject
   CompanyTestDataLoaderService(CompanyService companyService) {
      this.companyService = companyService
   }

   Stream<Company> stream(int number = 1) {
      return CompanyTestDataLoader.stream(number)
         .map { companyService.save(it) }
   }
}
