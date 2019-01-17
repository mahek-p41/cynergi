package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Verification
import com.hightouchinc.cynergi.middleware.repository.VerificationRepository
import groovy.transform.CompileStatic
import org.eclipse.collections.impl.factory.Sets

import javax.inject.Inject
import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class VerificationTestDataLoader {
   static Stream<Verification> stream(int number = 1, boolean generateAuto = true, boolean generateEmployment = true, boolean generateLandlord = true, boolean generateReferences = true) {
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def numberFaker = faker.number()
      final def chuckNorris = faker.chuckNorris()

      return IntStream.range(0, value).mapToObj {
         final def verification = new Verification(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            numberFaker.digits(6),
            chuckNorris.fact(),
            numberFaker.digits(6),
            OffsetDateTime.now(),
            numberFaker.digits(6),
            generateAuto ? VerificationAutoTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create VerificationAuto") } : null,
            generateEmployment ? VerificationEmploymentTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create VerificationEmployment") } : null,
            generateLandlord ? VerificationLandlordTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create VerificationLandlord") } : null,
            Sets.mutable.empty()
         )

         if (generateReferences) {
            verification.references.addAll(VerificationReferenceTestDataLoader.stream(verification, 6).collect(Collectors.toList()))
         }

         return verification
      }
   }
}

@Singleton
@CompileStatic
class VerificationDataLoaderService {
   private final VerificationRepository verificationRepository

   @Inject
   VerificationDataLoaderService(VerificationRepository verificationRepository) {
      this.verificationRepository = verificationRepository
   }

   Stream<Verification> stream(int number = 1, boolean generateAuto = true, boolean generateEmployment = true, generateLandlord = true) {
      return VerificationTestDataLoader.stream(number)
         .map {
            verificationRepository.insert(it)
         }
   }
}
