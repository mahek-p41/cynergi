package com.cynergisuite.middleware.region

import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

object RegionFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, division: DivisionEntity, regionalManager: EmployeeEntity? = null): Stream<RegionEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         RegionEntity(
            division = division,
            number = random.nextInt(1, 100_000),
            name = lorem.characters(5, 8).toUpperCase(),
            employeeNumber = regionalManager?.number,
            description = lorem.characters(3, 15).toUpperCase()
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class RegionFactoryService(
   private val regionRepository: RegionRepository
) {

   fun stream(numberIn: Int = 1, division: DivisionEntity): Stream<RegionEntity> =
      RegionFactory.stream(numberIn, division)
         .map { regionRepository.insert(it, division) }
}
