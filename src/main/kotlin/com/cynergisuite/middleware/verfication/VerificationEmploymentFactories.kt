package com.cynergisuite.middleware.verfication

import com.cynergisuite.middleware.verfication.infrastructure.VerificationEmploymentRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object VerificationEmploymentTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, verificationIn: Verification? = null): Stream<VerificationEmployment> {
      val verification = verificationIn ?: VerificationTestDataLoader.stream(1).findFirst().orElseThrow { Exception("Unable to create Verification") }
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val company = faker.company()
      val job = faker.job()
      val date = faker.date()
      val bool = faker.bool()

      return IntStream.range(0, number).mapToObj {
         VerificationEmployment(
            department = job.field(),
            hireDate = date.past(3650, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            leaveMessage = bool.bool(),
            name = company.name(),
            reliable = bool.bool(),
            title = job.title(),
            verification = verification
         )
      }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class VerificationEmploymentDataLoaderService @Inject constructor(
   private val verificationEmploymentRepository: VerificationEmploymentRepository,
   private val verificationDataLoaderService: VerificationDataLoaderService
) {

   fun stream(numberIn: Int): Stream<VerificationEmployment> {
      return stream(numberIn, null)
   }

   fun stream(numberIn: Int = 1, verificationIn: Verification? = null): Stream<VerificationEmployment> {
      val verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { Exception("Unable to create Verification") }

      return VerificationEmploymentTestDataLoader.stream(numberIn, verification)
         .map {
            verificationEmploymentRepository.insert(it)
         }
   }
}
