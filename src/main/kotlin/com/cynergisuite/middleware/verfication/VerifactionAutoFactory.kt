package com.cynergisuite.middleware.verfication

import com.cynergisuite.middleware.verfication.infrastructure.VerificationAutoRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object VerificationAutoTestDataLoader {

   fun stream(numberIn: Int = 1, verificationIn: Verification?): Stream<VerificationAuto> {
      val number = if (numberIn > 0) numberIn else 1
      val verification = verificationIn ?: VerificationTestDataLoader.stream(1).findFirst().orElseThrow { Exception("Unable to create Verification") }
      val faker = Faker()
      val bool = faker.bool()
      val lorem = faker.lorem()
      val phone = faker.phoneNumber()
      val address = faker.address()
      val name = faker.name()
      val date = faker.date()
      val num = faker.number()

      return IntStream.range(0, number).mapToObj {
         VerificationAuto(
            address = bool.bool(),
            comment = lorem.characters(3, 100),
            dealerPhone = phone.cellPhone(),
            diffAddress = address.streetAddress(false),
            diffEmployee = name.username(),
            diffPhone = phone.cellPhone(),
            dmvVerify = bool.bool(),
            employer = bool.bool(),
            lastPayment = date.past(28, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            name = name.fullName(),
            nextPayment = date.future(28, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            note = lorem.characters(3, 50),
            paymentFrequency = lorem.characters(3, 10),
            payment = num.randomDouble(2, 100, 1100).toBigDecimal(),
            pendingAction = lorem.characters(1, 50),
            phone = bool.bool(),
            previousLoan = bool.bool(),
            purchaseDate = date.past(90, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
            related = lorem.characters(1, 50),
            verification = verification
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class VerificationAutoDataLoaderService @Inject constructor(
   val verificationAutoRepository: VerificationAutoRepository,
   val verificationDataLoaderService: VerificationDataLoaderService
) {

   fun stream(numberIn: Int = 1): Stream<VerificationAuto> {
      return stream(numberIn, null)
   }

   fun stream(numberIn: Int = 1, verificationIn: Verification? = null): Stream<VerificationAuto> {
      val verification = verificationIn ?: verificationDataLoaderService.stream(1).findFirst().orElseThrow { Exception("Unable to create Verification") }

      return VerificationAutoTestDataLoader.stream(numberIn, verification)
         .map {
            verificationAutoRepository.insert(it)
         }
   }
}
