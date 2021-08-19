package com.cynergisuite.middleware.region

import com.cynergisuite.domain.SimpleLegacyIdentifiableDTO
import com.cynergisuite.middleware.division.DivisionDTO
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.region.infrastructure.RegionRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Inject
import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class RegionTestDataLoader {

   static Stream<RegionEntity> stream(int numberIn = 1, DivisionEntity divisionIn, EmployeeEntity regionalManager = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         final name = lorem.word().capitalize() + " Region"
         final description = "$name Description"

         new RegionEntity(
            null,
            it + 1,
            name,
            description,
            divisionIn,
            regionalManager,
         )
      }
   }

   static Stream<RegionDTO> streamDTO(int numberIn = 1, DivisionEntity divisionIn, EmployeeEntity regionalManager = null) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final lorem = faker.lorem()

      return IntStream.range(0, number).mapToObj {
         final name = lorem.word().capitalize() + " Region"
         final description = "$name Description"

         new RegionDTO(
            null,
            it + 1,
            name,
            description,
            new DivisionDTO(divisionIn),
            new SimpleLegacyIdentifiableDTO(regionalManager?.id)
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class RegionTestDataLoaderService {
   private final RegionRepository regionRepository

   @Inject
   RegionTestDataLoaderService(RegionRepository regionRepository) {
      this.regionRepository = regionRepository
   }

   Stream<RegionEntity> stream(int numberIn = 1, DivisionEntity division, EmployeeEntity regionalManager = null) {
      RegionTestDataLoader.stream(numberIn, division, regionalManager)
         .map { regionRepository.insert(it) }
   }

   RegionEntity single(DivisionEntity divisionIn) {
      stream(divisionIn).findFirst().orElseThrow { new Exception("Unable to create RegionEntity") }
   }

   RegionEntity single(DivisionEntity divisionIn, EmployeeEntity regionalManager) {
      stream(1, divisionIn, regionalManager).findFirst().orElseThrow { new Exception("Unable to create RegionEntity") }
   }

   RegionDTO singleDTO(DivisionEntity divisionIn, EmployeeEntity regionalManager) {
      RegionTestDataLoader.streamDTO(1, divisionIn, regionalManager).findFirst().orElseThrow { new Exception("Unable to create RegionEntity") }
   }
}
