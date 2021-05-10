package com.cynergisuite.middleware.region

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.division.DivisionDTO
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.LocalDate
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object RegionFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, divisionIn: DivisionEntity, regionalManager: EmployeeEntity? = null): Stream<RegionEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         val name = lorem.word().capitalize() + " Region"
         val description = "$name Description"
         RegionEntity(
            name = name,
            description = description,
            division = divisionIn,
            regionalManager = regionalManager,
            effectiveDate = LocalDate.MIN,
            endingDate = null,
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, divisionIn: DivisionEntity, regionalManager: EmployeeEntity? = null): Stream<RegionDTO> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         val name = lorem.word().capitalize() + " Region"
         val description = "$name Description"
         RegionDTO(
            name = name,
            description = description,
            division = DivisionDTO(divisionIn),
            regionalManager = SimpleIdentifiableDTO(regionalManager?.id)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class RegionFactoryService @Inject constructor(
   private val regionRepository: RegionRepository
) {
   fun stream(numberIn: Int = 1, division: DivisionEntity, regionalManager: EmployeeEntity? = null): Stream<RegionEntity> =
      RegionFactory.stream(numberIn, division, regionalManager)
         .map { regionRepository.insert(it) }

   fun single(divisionIn: DivisionEntity): RegionEntity =
      stream(division = divisionIn).findFirst().orElseThrow { Exception("Unable to create RegionEntity") }

   fun single(divisionIn: DivisionEntity, regionalManager: EmployeeEntity): RegionEntity =
      stream(division = divisionIn, regionalManager = regionalManager).findFirst().orElseThrow { Exception("Unable to create RegionEntity") }

   fun singleDTO(divisionIn: DivisionEntity, regionalManager: EmployeeEntity): RegionDTO =
      RegionFactory.streamDTO(1, divisionIn = divisionIn, regionalManager = regionalManager).findFirst().orElseThrow { Exception("Unable to create RegionEntity") }
}
