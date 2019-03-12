package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.Organization
import com.hightouchinc.cynergi.middleware.repository.CompanyRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class CompanyTestDataLoader {
   static Stream<Company> stream(int number = 1, Organization organization = null) {
      final int value = number > 0 ? number : 1
      final Organization companyOrganization = organization ?: OrganizationTestDataLoader.single()
      final Faker faker = new Faker()
      final company = faker.company()

      return IntStream.range(0, value).mapToObj {
         new Company(
            company.name(),
            companyOrganization
         )
      }
   }

   static Company single(Organization organization = null) {
      return stream(1, organization).findFirst().orElseThrow { new Exception("Unable to create Company") }
   }
}

@Singleton
@CompileStatic
class CompanyDataLoaderService {
   private final CompanyRepository companyRepository
   private final OrganizationDataLoaderService organizationDataLoaderService

   CompanyDataLoaderService(CompanyRepository companyRepository, OrganizationDataLoaderService organizationDataLoaderService) {
      this.companyRepository = companyRepository
      this.organizationDataLoaderService = organizationDataLoaderService
   }

   Stream<Company> stream(int number = 1, Organization organization = null) {
      final Organization companyOrganization = organization != null ? organization : organizationDataLoaderService.single()

      return CompanyTestDataLoader.stream(number, companyOrganization)
         .map {
            companyRepository.insert(it)
         }
   }

   Company single(Organization organization = null) {
      return stream(1, organization).findFirst().orElseThrow { new Exception("Unable to create Company") }
   }
}
