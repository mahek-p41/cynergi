package com.cynergisuite.test.data.loader


import com.github.javafaker.Faker
import com.cynergisuite.middleware.entity.Verification
import com.cynergisuite.middleware.entity.VerificationAuto
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class VerificationAutoTestDataLoader {
   static Stream<VerificationAuto> stream(int number = 1, Verification verificationIn = null) {
      final Verification verification = verificationIn ?: VerificationTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def bool = faker.bool()
      final def lorem = faker.lorem()
      final def phone = faker.phoneNumber()
      final def address = faker.address()
      final def name = faker.name()
      final def date = faker.date()
      final def num = faker.number()

      return IntStream.range(0, value).mapToObj {
         new VerificationAuto(
            null,
            UUID.randomUUID(),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            bool.bool(),
            lorem.characters(3, 100),
            phone.cellPhone(),
            address.streetAddress(false),
            name.username(),
            phone.cellPhone(),
            bool.bool(),
            bool.bool(),
            date.past(28, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            name.fullName(),
            date.future(28, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            lorem.characters(3, 50),
            lorem.characters(3, 10),
            num.randomDouble(2, 100, 1100).toBigDecimal(),
            lorem.characters(1, 50),
            bool.bool(),
            bool.bool(),
            date.past(90, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            lorem.characters(1, 50),
            verification
         )
      }
   }
}

@Singleton
@CompileStatic
class VerificationAutoDataLoaderService {
   private final com.cynergisuite.middleware.repository.VerificationAutoRepository verificationAutoRepository
   private final VerificationDataLoaderService verificationDataLoaderService

   VerificationAutoDataLoaderService(
      com.cynergisuite.middleware.repository.VerificationAutoRepository verificationAutoRepository,
      VerificationDataLoaderService verificationDataLoaderService
   ) {
      this.verificationAutoRepository = verificationAutoRepository
      this.verificationDataLoaderService = verificationDataLoaderService
   }

   Stream<VerificationAuto> stream(int number = 1, Verification verificationIn = null) {
      final Verification verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }

      return VerificationAutoTestDataLoader.stream(number, verification)
         .map {
            verificationAutoRepository.insert(it)
         }
   }
}
