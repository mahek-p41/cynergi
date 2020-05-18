package com.cynergisuite.middleware.region

import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object RegionFactory {

   @JvmStatic
   private val regionNumberCounter = AtomicLong(1)

   @JvmStatic
   fun stream(numberIn: Int = 1, divisionIn: DivisionEntity): Stream<RegionEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         val name = lorem.word().capitalize() + " Region"
         val description = "$name Description"
         RegionEntity(
            name = name,
            number = regionNumberCounter.getAndIncrement(),
            description = description,
            division = divisionIn
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class RegionFactoryService @Inject constructor(
   private val regionRepository: RegionRepository
) {
   fun stream(numberIn: Int = 1, division: DivisionEntity): Stream<RegionEntity> =
      RegionFactory.stream(numberIn, division)
         .map { regionRepository.insert(it) }

   fun single(divisionIn: DivisionEntity): RegionEntity =
      stream(division = divisionIn).findFirst().orElseThrow { Exception("Unable to create RegionEntity") }

}
