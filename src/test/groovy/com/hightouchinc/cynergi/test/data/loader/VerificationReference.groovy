package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.helper.EntityProxiedIdentifiableEntity
import com.hightouchinc.cynergi.middleware.entity.Verification
import com.hightouchinc.cynergi.middleware.entity.VerificationReference
import com.hightouchinc.cynergi.middleware.repository.VerificationReferenceRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class VerificationReferenceTestDataLoader {
   static Stream<VerificationReference> stream(Verification verification = null, int number = 1) {
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def bool = faker.bool()
      final def num = faker.number()
      final def lorem = faker.lorem()
      final Verification verificationRef = verification != null ? verification : VerificationTestDataLoader.stream(1, false, false, false, false).findFirst().orElseThrow { new Exception("Unable to create Verification") }

      return IntStream.range(0, value).mapToObj {
         new VerificationReference(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            bool.bool(),
            bool.bool(),
            num.numberBetween(1, 20),
            bool.bool(),
            lorem.fixedString(3),
            bool.bool(),
            bool.bool(),
            num.numberBetween(1, 20),
            bool.bool(),
            new EntityProxiedIdentifiableEntity(verificationRef)
         )
      }
   }
}

@Singleton
@CompileStatic
class VerificationReferenceDataLoaderService {
   private final VerificationDataLoaderService verificationDataLoaderService
   private final VerificationReferenceRepository verificationReferenceRepository

   VerificationReferenceDataLoaderService(
      VerificationDataLoaderService verificationDataLoaderService,
      VerificationReferenceRepository verificationReferenceRepository
   ) {
      this.verificationDataLoaderService = verificationDataLoaderService
      this.verificationReferenceRepository = verificationReferenceRepository
   }

   Stream<VerificationReference> stream(Verification verification = null, int number = 1) {
      verification = verification !=  null ? verification : verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }

      return VerificationReferenceTestDataLoader.stream(verification, number)
         .map {
            verificationReferenceRepository.insert(it)
         }
   }
}
