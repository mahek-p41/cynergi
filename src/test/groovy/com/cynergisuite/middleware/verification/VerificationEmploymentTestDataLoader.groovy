package com.cynergisuite.middleware.verification

import com.cynergisuite.middleware.verfication.Verification
import com.cynergisuite.middleware.verfication.VerificationEmployment
import com.cynergisuite.middleware.verfication.infrastructure.VerificationEmploymentRepository
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
class VerificationEmploymentTestDataLoader {

   static Stream<VerificationEmployment> stream(int numberIn = 1, Verification verificationIn = null) {
      final verification = verificationIn ?: VerificationTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final company = faker.company()
      final job = faker.job()
      final date = faker.date()
      final bool = faker.bool()

      return IntStream.range(0, number).mapToObj {
         new VerificationEmployment(
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            job.field(),
            date.past(3650, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            bool.bool(),
            company.name(),
            bool.bool(),
            job.title(),
            verification
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class VerificationEmploymentTestDataLoaderService {
   private final VerificationEmploymentRepository verificationEmploymentRepository
   private final VerificationTestDataLoaderService verificationDataLoaderService

   VerificationEmploymentTestDataLoaderService(VerificationEmploymentRepository verificationEmploymentRepository, VerificationTestDataLoaderService verificationDataLoaderService) {
      this.verificationEmploymentRepository = verificationEmploymentRepository
      this.verificationDataLoaderService = verificationDataLoaderService
   }

   Stream<VerificationEmployment> stream(int numberIn = 1, Verification verificationIn = null) {
      final verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }

      return VerificationEmploymentTestDataLoader.stream(numberIn, verification)
         .map {
            verificationEmploymentRepository.insert(it)
         }
   }
}
