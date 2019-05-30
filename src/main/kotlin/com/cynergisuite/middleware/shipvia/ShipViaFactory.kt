package com.cynergisuite.middleware.shipvia

import com.cynergisuite.middleware.shipvia.infrastructure.ShipViaRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object ShipViaFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<ShipVia> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         ShipVia(
            shipViaName = lorem.characters(3, 250),
            shipViaDescription = lorem.characters(3, 495)
         )
      }
   }

   @JvmStatic
   fun single(): ShipVia {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create ShipVia") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class ShipViaFactoryService @Inject constructor(
   private val shipViaRepository: ShipViaRepository
) {
   fun stream(numberIn: Int = 1): Stream<ShipVia> {
      return ShipViaFactory.stream(numberIn).map {
         shipViaRepository.insert(it)
      }
   }

   fun single(): ShipVia {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create ShipVia")}
   }
}
