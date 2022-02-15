package com.cynergisuite.middleware.verification

import com.cynergisuite.middleware.verfication.Verification
import com.cynergisuite.middleware.verfication.VerificationReference
import com.cynergisuite.middleware.verfication.infrastructure.VerificationReferenceRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream
import jakarta.inject.Singleton

@CompileStatic
class VerificationReferenceTestDataLoader {

   static Stream<VerificationReference> stream(int numberIn = 1, Verification verificationIn = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final bool = faker.bool()
      final num = faker.number()
      final lorem = faker.lorem()
      final verification = verificationIn ?: VerificationTestDataLoader.stream(1, false, false, false).findFirst().orElseThrow { new Exception("Unable to create Verification") }

      return IntStream.range(0, number).mapToObj {
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
            verification
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class VerificationReferenceTestDataLoaderService {
   private final VerificationTestDataLoaderService verificationDataLoaderService
   private final VerificationReferenceRepository verificationReferenceRepository

   VerificationReferenceTestDataLoaderService(VerificationTestDataLoaderService verificationDataLoaderService, VerificationReferenceRepository verificationReferenceRepository) {
      this.verificationDataLoaderService = verificationDataLoaderService
      this.verificationReferenceRepository = verificationReferenceRepository
   }

   Stream<VerificationReference> stream(int numberIn = 1, Verification verificationIn = null) {
      final verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }

      return VerificationReferenceTestDataLoader.stream(numberIn, verification)
         .map {
            verificationReferenceRepository.insert(it)
         }
   }
}
