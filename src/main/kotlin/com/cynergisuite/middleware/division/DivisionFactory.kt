package com.cynergisuite.middleware.division

import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object DivisionFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, companyIn: CompanyEntity, divisionalManagerIn: EmployeeEntity? = null): Stream<DivisionEntity> {
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, numberIn).mapToObj {
         val name = lorem.word().capitalize() + " Division"
         val description = "$name Description"
         DivisionEntity(
            name = name,
            description = description,
            company = companyIn,
            divisionalManager = divisionalManagerIn
         )
      }
   }

   @JvmStatic
   fun streamDTO(numberIn: Int = 1, divisionalManagerIn: EmployeeEntity? = null): Stream<DivisionDTO> {
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, numberIn).mapToObj {
         val name = lorem.word().capitalize() + " Division"
         val description = "$name Description"
         DivisionDTO(
            name = name,
            description = description,
            divisionalManager = SimpleIdentifiableDTO(divisionalManagerIn?.id)
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class DivisionFactoryService @Inject constructor(
   private val divisionRepository: DivisionRepository
) {
   fun stream(numberIn: Int = 1, companyIn: CompanyEntity, divisionalManagerIn: EmployeeEntity? = null): Stream<DivisionEntity> =
      DivisionFactory.stream(numberIn, companyIn, divisionalManagerIn)
         .map { divisionRepository.insert(it) }

   fun single(companyIn: CompanyEntity): DivisionEntity =
      stream(companyIn = companyIn).findFirst().orElseThrow { Exception("Unable to create DivisionEntity") }

   fun single(companyIn: CompanyEntity, divisionalManagerIn: EmployeeEntity): DivisionEntity =
      stream(companyIn = companyIn, divisionalManagerIn = divisionalManagerIn).findFirst().orElseThrow { Exception("Unable to create DivisionEntity") }

   fun singleDTO(company: CompanyEntity, divisionalManagerIn: EmployeeEntity): DivisionDTO {
      return DivisionFactory.streamDTO(1, divisionalManagerIn).findFirst().orElseThrow { Exception("Unable to create DivisionDTO") }
   }
}
