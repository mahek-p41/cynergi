package com.cynergisuite.middleware.verification

import com.cynergisuite.middleware.verfication.Verification
import com.cynergisuite.middleware.verfication.VerificationAuto
import com.cynergisuite.middleware.verfication.infrastructure.VerificationAutoRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

@CompileStatic
class VerificationAutoTestDataLoader {

   static Stream<VerificationAuto> stream(int numberIn = 1, Verification verificationIn = null) {
      final number = numberIn > 0 ? numberIn : 1
      final verification = verificationIn ?: VerificationTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final faker = new Faker()
      final bool = faker.bool()
      final lorem = faker.lorem()
      final phone = faker.phoneNumber()
      final address = faker.address()
      final name = faker.name()
      final date = faker.date()
      final num = faker.number()

      return IntStream.range(0, number).mapToObj {
         new VerificationAuto(
            null,
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
@Requires(env = ["develop", "test"])
class VerificationAutoTestDataLoaderService {
   private final VerificationAutoRepository verificationAutoRepository
   private final VerificationTestDataLoaderService verificationDataLoaderService

   VerificationAutoTestDataLoaderService(VerificationAutoRepository verificationAutoRepository, VerificationTestDataLoaderService verificationDataLoaderService) {
      this.verificationAutoRepository = verificationAutoRepository
      this.verificationDataLoaderService = verificationDataLoaderService
   }

   Stream<VerificationAuto> stream(int numberIn = 1, Verification verificationIn = null) {
      final verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }

      return VerificationAutoTestDataLoader.stream(numberIn, verification)
         .map {
            verificationAutoRepository.insert(it)
         }
   }
}
