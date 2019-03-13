package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.Organization
import com.hightouchinc.cynergi.middleware.repository.CompanyRepository
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

object CompanyTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, organizationIn: Organization?): Stream<Company> {
      val number = if (numberIn > 0) numberIn else 1
      val organization = organizationIn ?: OrganizationTestDataLoader.single()
      val faker = Faker()
      val company = faker.company()

      return IntStream.range(0, number).mapToObj {
         Company(
            name = company.name(),
            organization = organization
         )
      }
   }

   @JvmStatic
   fun single(organizationIn: Organization?): Company {
      return stream(organizationIn = organizationIn).findFirst().orElseThrow { Exception("Unable to create Company") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class CompanyDataLoaderService(
   private val companyRepository: CompanyRepository,
   private val organizationDataLoaderService: OrganizationDataLoaderService
) {

   fun stream(numberIn: Int = 1, organizationIn: Organization?): Stream<Company> {
      val organization = organizationIn ?: organizationDataLoaderService.single()

      return CompanyTestDataLoader.stream(numberIn = numberIn, organizationIn = organization)
         .map { companyRepository.insert(it) }
   }

   fun single(organizationIn: Organization?): Company {
      return stream(organizationIn = organizationIn).findFirst().orElseThrow { Exception("Unable to create Company") }
   }
}
