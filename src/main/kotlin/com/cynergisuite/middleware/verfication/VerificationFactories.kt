package com.cynergisuite.middleware.verfication

import com.cynergisuite.middleware.verfication.infrastructure.VerificationRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.OffsetDateTime
import java.util.stream.Collectors.toList
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object VerificationTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<Verification> {
      return stream(numberIn, generateAuto = true, generateEmployment = true, generateLandlord = true, generateReferences = true)
   }

   @JvmStatic
   fun stream(numberIn: Int = 1, generateAuto: Boolean = true, generateEmployment: Boolean = true, generateLandlord: Boolean = true, generateReferences: Boolean = true): Stream<Verification> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val num = faker.number()
      val chuckNorris = faker.chuckNorris()

      return IntStream.range(0, number).mapToObj {
         val verification = Verification(
            customerAccount = num.digits(6),
            customerComments = chuckNorris.fact(),
            verifiedBy = num.digits(6),
            verifiedTime = OffsetDateTime.now(),
            company = num.digits(6),
            auto = null,
            employment = null,
            landlord = null,
            references = mutableListOf()
         )

         verification.auto = if (generateAuto) VerificationAutoTestDataLoader.stream(1, verification).findFirst().orElseThrow { Exception("Unable to create VerificationAuto") } else null
         verification.employment = if (generateEmployment) VerificationEmploymentTestDataLoader.stream(1, verification).findFirst().orElseThrow { Exception("Unable to create VerificationEmployment") } else null
         verification.landlord = if (generateLandlord) VerificationLandlordTestDataLoader.stream(1, verification).findFirst().orElseThrow { Exception("Unable to create VerificationLandlord") } else null

         if (generateReferences) {
            verification.references.addAll(VerificationReferenceTestDataLoader.stream(6, verification).collect(toList()))
         }

         verification
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class VerificationDataLoaderService @Inject constructor(
   private val verificationRepository: VerificationRepository
) {
   fun stream(numberIn: Int = 1): Stream<Verification> {
      return stream(numberIn, generateAuto = true, generateEmployment = true, generateLandlord = true, generateReferences = true)
   }

   fun stream(numberIn: Int = 1, generateAuto: Boolean = true, generateEmployment: Boolean = true, generateLandlord: Boolean = true, generateReferences: Boolean = true): Stream<Verification> {
      return VerificationTestDataLoader.stream(numberIn, generateAuto, generateEmployment, generateLandlord, generateReferences)
         .map {
            verificationRepository.insert(it)
         }
   }
}
