package com.cynergisuite.middleware.verfication

import com.cynergisuite.middleware.verfication.infrastructure.VerificationLandlordRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object VerificationLandlordTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, verificationIn: Verification? = null): Stream<VerificationLandlord> {
      val verification = verificationIn ?: VerificationTestDataLoader.stream(1).findFirst().orElseThrow { Exception("Unable to create Verification") }
      val value = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val bool = faker.bool()
      val phone = faker.phoneNumber()
      val lorem = faker.lorem()
      val num = faker.number()
      val name = faker.name()

      return IntStream.range(0, value).mapToObj {
         VerificationLandlord(
            address = bool.bool(),
            altPhone = phone.cellPhone(),
            leaseType = lorem.characters(10),
            leaveMessage = bool.bool(),
            length = num.randomNumber(3, true).toString() + " " + lorem.characters(5),
            name = name.name(),
            paidRent = lorem.characters(10),
            phone = bool.bool(),
            reliable = bool.bool(),
            rent = num.randomDouble(2, 100, 10000).toBigDecimal(),
            verification = verification
         )
      }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class VerificationLandlordDataLoaderService @Inject constructor(
   private val verificationLandlordRepository: VerificationLandlordRepository,
   private val verificationDataLoaderService: VerificationDataLoaderService
) {

   fun stream(numberIn: Int = 1): Stream<VerificationLandlord>  {
      return stream(numberIn, null)
   }

   fun stream(numberIn: Int = 1, verificationIn: Verification? = null): Stream<VerificationLandlord>  {
      val verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { Exception("Unable to create Verification") }

      return VerificationLandlordTestDataLoader.stream(numberIn, verification)
         .map {
            verificationLandlordRepository.insert(it)
         }
   }
}
