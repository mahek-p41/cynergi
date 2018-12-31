package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.repository.CompanyRepository
import groovy.transform.CompileStatic

import javax.inject.Inject
import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class CompanyTestDataLoader {
   static Stream<Company> stream(int number = 1) {
      final int value = number > 0 ? number : 1
      final companyFaker = new Faker().company()

      return IntStream.range(0, value).mapToObj {
         new Company(companyFaker.name())
      }
   }
}

@Singleton
@CompileStatic
class CompanyTestDataLoaderService {
   private final CompanyRepository companyRepository

   @Inject
   CompanyTestDataLoaderService(CompanyRepository companyRepository) {
      this.companyRepository = companyRepository
   }

   Stream<Company> stream(int number = 1) {
      return CompanyTestDataLoader.stream(number)
         .map { companyRepository.save(it) }
   }
}
