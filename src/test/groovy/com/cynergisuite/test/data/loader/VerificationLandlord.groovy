package com.cynergisuite.test.data.loader

import com.github.javafaker.Faker
import com.cynergisuite.middleware.entity.Verification
import com.cynergisuite.middleware.entity.VerificationLandlord
import com.cynergisuite.middleware.repository.VerificationLandlordRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class VerificationLandlordTestDataLoader {
   static Stream<VerificationLandlord> stream(int number = 1, Verification verificationIn = null) {
      final Verification verification = verificationIn ?: VerificationTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def bool = faker.bool()
      final def phone = faker.phoneNumber()
      final def lorem = faker.lorem()
      final def num = faker.number()
      final def name = faker.name()

      return IntStream.range(0, value).mapToObj {
         new VerificationLandlord(
            null,
            UUID.randomUUID(),
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
class VerificationLandlordDataLoaderService {
   private final VerificationLandlordRepository verificationLandlordRepository
   private final VerificationDataLoaderService verificationDataLoaderService

   VerificationLandlordDataLoaderService(
      VerificationLandlordRepository verificationLandlordRepository,
      VerificationDataLoaderService verificationDataLoaderService
   ) {
      this.verificationLandlordRepository = verificationLandlordRepository
      this.verificationDataLoaderService = verificationDataLoaderService
   }

   Stream<VerificationLandlord> stream(int number = 1, Verification verificationIn = null) {
      final Verification verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }

      return VerificationLandlordTestDataLoader.stream(number, verification)
         .map {
            verificationLandlordRepository.insert(it)
         }
   }
}
