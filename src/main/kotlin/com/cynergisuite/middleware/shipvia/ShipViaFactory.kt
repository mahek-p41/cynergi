package com.cynergisuite.middleware.shipvia

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
   fun stream(numberIn: Int = 1, datasetIn: String? = null): Stream<ShipViaEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val dataset = datasetIn ?: CompanyFactory.random().datasetCode

      return IntStream.range(0, number).mapToObj {
         ShipViaEntity(
            description = lorem.characters(3, 30),
            dataset = dataset
         )
      }
   }

   @JvmStatic
   fun single(): ShipViaEntity {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create ShipVia") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class ShipViaFactoryService @Inject constructor(
   private val companyFactoryService: CompanyFactoryService,
   private val shipViaRepository: ShipViaRepository
) {
   fun stream(numberIn: Int = 1, datasetIn: String? = null): Stream<ShipViaEntity> {
      val dataset = datasetIn ?: companyFactoryService.random().datasetCode
      return ShipViaFactory.stream(numberIn, dataset).map {
         shipViaRepository.insert(it)
      }
   }

   fun single(): ShipViaEntity {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create ShipVia")}
   }

   fun single(datasetIn: String?): ShipViaEntity {
      return stream(1, datasetIn).findFirst().orElseThrow { Exception("Unable to create ShipVia")}
   }
}
