package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactory
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object EmployeeFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, storeIn: StoreEntity? = null): Stream<EmployeeEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val bool = faker.bool()
      val numbers = faker.number()
      val name = faker.name()
      val store = storeIn ?: StoreFactory.random()

      return IntStream.range(0, number).mapToObj {
         EmployeeEntity(
            type = "eli",
            number = numbers.numberBetween(1, 10_000),
            dataset = store.dataset,
            lastName = name.lastName(),
            firstNameMi = name.firstName(),
            passCode = "password",
            store = store,
            active = true
         )
      }
   }

   @JvmStatic
   fun single(): EmployeeEntity =
      stream(1).findFirst().orElseThrow { Exception("Unable to create Employee") }

   @JvmStatic
   fun testEmployee(): EmployeeEntity =
      EmployeeEntity(
         id = 1,
         type = "eli",
         number = 111,
         dataset = "tstds1",
         lastName = "MARTINEZ",
         firstNameMi = "DANIEL",
         passCode = "pass",
         store = StoreEntity(
            id = 1,
            number = 1,
            name = "KANSAS CITY",
            dataset = "testds"
         ),
         active = true
      )

}

@Singleton
@Requires(env = ["develop", "test"])
class EmployeeFactoryService @Inject constructor(
   private val employeeRepository: EmployeeRepository
) {

   fun stream(numberIn: Int = 1, storeIn: StoreEntity? = null): Stream<EmployeeEntity> {
      return EmployeeFactory.stream(numberIn, storeIn)
         .map {
            employeeRepository.insert(it)
         }
   }

   fun single(): EmployeeEntity {
      return single(null)
   }

   fun single(storeIn: StoreEntity? = null): EmployeeEntity {
      return stream(1, storeIn).findFirst().orElseThrow { Exception("Unable to create Employee") }
   }

   fun findTestEmployee(number: Int, dataset: String): EmployeeEntity? {
      return employeeRepository.findOne(number, "sysz", dataset)
   }
}
