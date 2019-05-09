package com.cynergisuite.middleware.verfication

import com.cynergisuite.middleware.verfication.infrastructure.VerificationReferenceRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object VerificationReferenceTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, verificationIn: Verification? = null): Stream<VerificationReference> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val bool = faker.bool()
      val num = faker.number()
      val lorem = faker.lorem()
      val verification = verificationIn ?: VerificationTestDataLoader.stream(1, generateAuto = false, generateEmployment = false, generateLandlord = false).findFirst().orElseThrow { Exception("Unable to create Verification") }

      return IntStream.range(0, number).mapToObj {
         VerificationReference(
            address = bool.bool(),
            hasHomePhone = bool.bool(),
            known = num.numberBetween(1, 20),
            leaveMessage = bool.bool(),
            rating =  lorem.fixedString(3),
            relationship = bool.bool(),
            reliable = bool.bool(),
            timeFrame = num.numberBetween(1, 20),
            verifyPhone = bool.bool(),
            verification = verification
         )
      }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class VerificationReferenceDataLoaderService @Inject constructor(
   val verificationDataLoaderService: VerificationDataLoaderService,
   val verificationReferenceRepository: VerificationReferenceRepository
) {
   fun stream(numberIn: Int = 1, verificationIn: Verification? = null): Stream<VerificationReference> {
      val verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { Exception("Unable to create Verification") }

      return VerificationReferenceTestDataLoader.stream(numberIn, verification)
         .map {
            verificationReferenceRepository.insert(it)
         }
   }
}
