package com.cynergisuite.middleware.division

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class DivisionFactory {

   static Stream<DivisionEntity> stream(int numberIn = 1, CompanyEntity companyIn) {
      final faker = new Faker()
      final lorem = faker.lorem()

      return IntStream.range(0, numberIn).mapToObj {
         final name = lorem.word().capitalize() + " Division"
         final description = "$name Description"

         new DivisionEntity(
            null,
            companyIn,
            it + 1,
            name,
            description,
         )
      }
   }

   static Stream<DivisionDTO> streamDTO(int numberIn = 1) {
      final faker = new Faker()
      final lorem = faker.lorem()

      return IntStream.range(0, numberIn).mapToObj {
         final name = lorem.word().capitalize() + " Division"
         final description = "$name Description"

         new DivisionDTO(
            null,
            it + 1,
            name,
            description
         )
      }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class DivisionFactoryService {
   private final DivisionRepository divisionRepository

   DivisionFactoryService(DivisionRepository divisionRepository) {
      this.divisionRepository = divisionRepository
   }

   Stream<DivisionEntity> stream(int numberIn = 1, CompanyEntity companyIn) {
      DivisionFactory.stream(numberIn, companyIn)
         .map { divisionRepository.insert(it) }
   }

   DivisionEntity single(CompanyEntity companyIn) {
      stream(companyIn).findFirst().orElseThrow { new Exception("Unable to create DivisionEntity") }
   }

   DivisionDTO singleDTO() {
      return DivisionFactory.streamDTO(1).findFirst().orElseThrow { new Exception("Unable to create DivisionDTO") }
   }
}
