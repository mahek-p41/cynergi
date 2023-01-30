package com.cynergisuite.middleware.verification

import com.cynergisuite.middleware.verfication.Verification
import com.cynergisuite.middleware.verfication.VerificationLandlord
import com.cynergisuite.middleware.verfication.infrastructure.VerificationLandlordRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class VerificationLandlordTestDataLoader {

   static Stream<VerificationLandlord> stream(int numberIn = 1, Verification verificationIn = null) {
      final verification = verificationIn ?: VerificationTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final bool = faker.bool()
      final phone = faker.phoneNumber()
      final lorem = faker.lorem()
      final num = faker.number()
      final name = faker.name()

      return IntStream.range(0, number).mapToObj {
         new VerificationLandlord(
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            bool.bool(),
            phone.cellPhone(),
            lorem.characters(10),
            bool.bool(),
            num.randomNumber(3, true).toString() + " " + lorem.characters(5),
            name.name(),
            lorem.characters(10),
            bool.bool(),
            bool.bool(),
            num.randomDouble(2, 100, 10000).toBigDecimal(),
            verification
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class VerificationLandlordTestDataLoaderService {
   private final VerificationLandlordRepository verificationLandlordRepository
   private final VerificationTestDataLoaderService verificationDataLoaderService

   @Inject
   VerificationLandlordTestDataLoaderService(VerificationLandlordRepository verificationLandlordRepository, VerificationTestDataLoaderService verificationDataLoaderService) {
      this.verificationLandlordRepository = verificationLandlordRepository
      this.verificationDataLoaderService = verificationDataLoaderService
   }

   Stream<VerificationLandlord> stream(int numberIn = 1, Verification verificationIn = null) {
      final verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }

      return VerificationLandlordTestDataLoader.stream(numberIn, verification)
         .map {
            verificationLandlordRepository.insert(it)
         }
   }
}
