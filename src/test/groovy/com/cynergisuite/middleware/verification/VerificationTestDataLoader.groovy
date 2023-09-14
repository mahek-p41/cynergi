package com.cynergisuite.middleware.verification

import com.cynergisuite.middleware.verfication.Verification
import com.cynergisuite.middleware.verfication.VerificationAuto
import com.cynergisuite.middleware.verfication.VerificationEmployment
import com.cynergisuite.middleware.verfication.VerificationLandlord
import com.cynergisuite.middleware.verfication.infrastructure.VerificationRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class VerificationTestDataLoader {

   static Stream<Verification> stream(int numberIn = 1, boolean generateAuto = true, boolean generateEmployment = true, boolean generateLandlord = true, boolean generateReferences = true) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final num = faker.number()
      final chuckNorris = faker.chuckNorris()

      return IntStream.range(0, number).mapToObj {
         final verification = new Verification(
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            num.digits(6),
            chuckNorris.fact(),
            num.digits(6),
            LocalDate.now(),
            num.digits(6),
            null,
            null,
            null,
            []
         )

         verification.auto = (generateAuto ? VerificationAutoTestDataLoader.stream(1, verification).findFirst().orElseThrow { new Exception("Unable to create VerificationAuto") } : null) as VerificationAuto
         verification.employment = (generateEmployment ? VerificationEmploymentTestDataLoader.stream(1, verification).findFirst().orElseThrow { new Exception("Unable to create VerificationEmployment") } : null) as VerificationEmployment
         verification.landlord = (generateLandlord ? VerificationLandlordTestDataLoader.stream(1, verification).findFirst().orElseThrow { new Exception("Unable to create VerificationLandlord") } : null) as VerificationLandlord

         if (generateReferences) {
            verification.references.addAll(VerificationReferenceTestDataLoader.stream(6, verification).collect(Collectors.toList()))
         }

         verification
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class VerificationTestDataLoaderService {
   private final VerificationRepository verificationRepository

   @Inject
   VerificationTestDataLoaderService(VerificationRepository verificationRepository) {
      this.verificationRepository = verificationRepository
   }

   Stream<Verification> stream(int numberIn = 1, boolean generateAuto = true, boolean generateEmployment = true, boolean generateLandlord = true, boolean generateReferences = true) {
      return VerificationTestDataLoader.stream(numberIn, generateAuto, generateEmployment, generateLandlord, generateReferences)
         .map {
            verificationRepository.insert(it)
         }
   }
}
