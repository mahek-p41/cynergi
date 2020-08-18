package com.cynergisuite.middleware.shipping.shipvia

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.shipping.shipvia.infrastructure.ShipViaRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import org.apache.commons.lang3.StringUtils
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object ShipViaTestDataLoader {

   @JvmStatic
   fun stream(numberIn: Int = 1, company: Company): Stream<ShipViaEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         ShipViaEntity(
            description = StringUtils.join(lorem.words(2), " ").capitalize(),
            company = company
         )
      }
   }

   @JvmStatic
   fun single(company: Company): ShipViaEntity {
      return stream(company = company).findFirst().orElseThrow { Exception("Unable to create ShipViaEntity") }
   }
}

@Singleton
@Requires(env = ["develop", "demo", "test"])
class ShipViaTestDataLoaderService @Inject constructor(
   private val shipViaRepository: ShipViaRepository
) {

   fun stream(numberIn: Int = 1, company: Company): Stream<ShipViaEntity> {
      return ShipViaTestDataLoader.stream(numberIn, company).map {
         shipViaRepository.insert(it)
      }
   }

   fun single(company: Company): ShipViaEntity {
      return stream(1, company).findFirst().orElseThrow { Exception("Unable to create ShipVia") }
   }
}
