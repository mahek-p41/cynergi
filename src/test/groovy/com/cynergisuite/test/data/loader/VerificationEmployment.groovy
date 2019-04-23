package com.cynergisuite.test.data.loader

import com.github.javafaker.Faker
import com.cynergisuite.middleware.entity.Verification
import com.cynergisuite.middleware.entity.VerificationEmployment
import com.cynergisuite.middleware.repository.VerificationEmploymentRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class VerificationEmploymentTestDataLoader {
   static Stream<VerificationEmployment> stream(int number = 1, Verification verificationIn = null) {
      final Verification verification = verificationIn ?: VerificationTestDataLoader.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }
      final int value = number > 0 ? number : 1
      final def faker = new Faker()
      final def company = faker.company()
      final def job = faker.job()
      final def date = faker.date()
      final def bool = faker.bool()

      return IntStream.range(0, value).mapToObj {
         new VerificationEmployment(
            null,
            UUID.randomUUID(),
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
class VerificationEmploymentDataLoaderService {
   private final VerificationEmploymentRepository verificationEmploymentRepository
   private final VerificationDataLoaderService verificationDataLoaderService

   VerificationEmploymentDataLoaderService(
      VerificationEmploymentRepository verificationEmploymentRepository,
      VerificationDataLoaderService verificationDataLoaderService
   ) {
      this.verificationEmploymentRepository = verificationEmploymentRepository
      this.verificationDataLoaderService = verificationDataLoaderService
   }

   Stream<VerificationEmployment> stream(int number = 1, Verification verificationIn = null) {
      final Verification verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { new Exception("Unable to create Verification") }

      return VerificationEmploymentTestDataLoader.stream(number, verification)
         .map {
            verificationEmploymentRepository.insert(it)
         }
   }
}
