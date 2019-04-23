package com.cynergisuite.test.data.loader

import com.cynergisuite.middleware.verfication.infrastructure.VerificationRepository
import com.github.javafaker.Faker
import com.cynergisuite.middleware.verfication.Verification
import com.cynergisuite.middleware.verfication.VerificationAuto
import com.cynergisuite.middleware.verfication.VerificationEmployment
import com.cynergisuite.middleware.verfication.VerificationLandlord
import com.cynergisuite.middleware.verfication.VerificationReference
import groovy.transform.CompileStatic

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
            null,
            null,
            null,
            new ArrayList<VerificationReference>()
         )

         verification.auto = generateAuto ? VerificationAutoTestDataLoader.stream(1, verification).findFirst().orElseThrow { new Exception("Unable to create VerificationAuto") } : null as VerificationAuto
         verification.employment = generateEmployment ? VerificationEmploymentTestDataLoader.stream(1, verification).findFirst().orElseThrow { new Exception("Unable to create VerificationEmployment") } : null as VerificationEmployment
         verification.landlord = generateLandlord ? VerificationLandlordTestDataLoader.stream(1, verification).findFirst().orElseThrow { new Exception("Unable to create VerificationLandlord") } : null as VerificationLandlord

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
   Stream<Verification> stream(int number = 1, boolean generateAuto = true, boolean generateEmployment = true, boolean generateLandlord = true, boolean generateReferences = true) {
      return VerificationTestDataLoader.stream(number, generateAuto, generateEmployment, generateLandlord, generateReferences)
         .map {
            verificationRepository.insert(it)
         }
   }
}
