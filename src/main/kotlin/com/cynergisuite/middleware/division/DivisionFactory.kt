package com.cynergisuite.middleware.division

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.infrastructure.CompanyRepository
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.cynergisuite.middleware.employee.EmployeeEntity

import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Singleton

object DivisionFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, company: Company, divisionalManager: EmployeeEntity? = null): Stream<DivisionEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val random = faker.random()

      return IntStream.range(0, number).mapToObj {
         DivisionEntity(
            company = company,
            number = random.nextInt(1, 100_000),
            name = lorem.characters(5, 8).toUpperCase(),
            manager = divisionalManager,
            description = lorem.characters(3, 15).toUpperCase()
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class DivisionFactoryService(
   private val divisionRepository: DivisionRepository
) {

   fun stream(numberIn: Int = 1, company: Company): Stream<DivisionEntity> =
      DivisionFactory.stream(numberIn, company)
         .map { divisionRepository.insert(it, company) }

   fun single(company: Company): DivisionEntity =
      stream(company = company).findFirst().orElseThrow { Exception("Unable to create DivisionEntity") }
}
