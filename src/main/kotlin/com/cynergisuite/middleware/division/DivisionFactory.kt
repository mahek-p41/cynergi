package com.cynergisuite.middleware.division

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.division.infrastructure.DivisionRepository
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object DivisionFactory {

   @JvmStatic
   private val divisionNumberCounter = AtomicInteger(1)

   @JvmStatic
   fun stream(numberIn: Int = 1, companyIn: CompanyEntity, divisionalManager: EmployeeEntity? = null): Stream<DivisionEntity> {
      val faker = Faker()
      val lorem = faker.lorem()

      return IntStream.range(0, numberIn).mapToObj {
         val name = lorem.word().capitalize() + " Division"
         val description = "$name Description"
         DivisionEntity(
            name = name,
            number = divisionNumberCounter.getAndIncrement(),
            manager = divisionalManager,
            description = description,
            company = companyIn
         )
      }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class DivisionFactoryService @Inject constructor(
   private val divisionRepository: DivisionRepository
) {
   fun stream(numberIn: Int = 1, company: CompanyEntity): Stream<DivisionEntity> =
      DivisionFactory.stream(numberIn, company)
         .map { divisionRepository.insert(it) }

   fun single(companyIn: CompanyEntity): DivisionEntity =
      stream(company = companyIn).findFirst().orElseThrow { Exception("Unable to create DivisionEntity") }

}
