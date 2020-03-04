package com.cynergisuite.middleware.shipvia

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.shipvia.infrastructure.ShipViaRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object ShipViaFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, company: Company): Stream<ShipViaEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         ShipViaEntity(
            description = lorem.characters(3, 30),
            number = random.nextInt(1, 1000),
            company = company
         )
      }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class ShipViaFactoryService @Inject constructor(
   private val companyFactoryService: CompanyFactoryService,
   private val shipViaRepository: ShipViaRepository
) {

   fun stream(numberIn: Int = 1, company: Company): Stream<ShipViaEntity> {
      return ShipViaFactory.stream(numberIn, company).map {
         shipViaRepository.insert(it)
      }
   }

   fun single(company: Company): ShipViaEntity {
      return stream(1, company).findFirst().orElseThrow { Exception("Unable to create ShipVia")}
   }
}
