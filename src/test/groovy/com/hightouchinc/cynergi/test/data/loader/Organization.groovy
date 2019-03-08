package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Organization
import com.hightouchinc.cynergi.middleware.repository.OrganizationRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class OrganizationTestDataLoader {
   static Stream<Organization> stream(int number = 1) {
      final int value = number > 0 ? number : 1
      final Faker faker = new Faker()
      final lorem = faker.lorem()

      return IntStream.range(0, value).mapToObj {
         new Organization(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            lorem.fixedString(6),
            lorem.characters(1, 50)
         )
      }
   }

   static Organization single() {
      return stream(1).findFirst().orElseThrow { new Exception("Unable to create Organization") }
   }
}

@Singleton
@CompileStatic
class OrganizationDataLoaderService {
   private final OrganizationRepository organizationRepository

   OrganizationDataLoaderService(OrganizationRepository organizationRepository) {
      this.organizationRepository = organizationRepository
   }

   Stream<Organization> stream(int number = 1) {
      return OrganizationTestDataLoader.stream(number)
         .map {
            organizationRepository.insert(it)
         }
   }

   Organization single() {
      return stream(1).findFirst().orElseThrow { new Exception("Unable to create Organization") }
   }
}
