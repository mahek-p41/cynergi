package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactory
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.time.OffsetDateTime
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object EmployeeFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, storeIn: StoreEntity? = null): Stream<Employee> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val bool = faker.bool()
      val numbers = faker.number()
      val name = faker.name()
      val store = storeIn ?: StoreFactory.random()

      return IntStream.range(0, number).mapToObj {
         Employee(
            loc = "int",
            number = numbers.numberBetween(1, 10_000),
            lastName = name.lastName(),
            firstNameMi = name.firstName(),
            passCode = lorem.characters(3, 6),
            store = store,
            active = bool.bool()
         )
      }
   }

   @JvmStatic
   fun single(): Employee =
      stream(1).findFirst().orElseThrow { Exception("Unable to create Employee") }

   @JvmStatic
   fun testEmployee(): Employee =
      Employee(
         id = 1,
         timeCreated = OffsetDateTime.now(),
         timeUpdated = OffsetDateTime.now(),
         loc = "int",
         number = 123,
         lastName = "user",
         firstNameMi = "test",
         passCode = "pass",
         store = StoreEntity(
            id = 1,
            timeCreated = OffsetDateTime.now(),
            timeUpdated = OffsetDateTime.now(),
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

   fun stream(numberIn: Int = 1, storeIn: StoreEntity? = null): Stream<Employee> {
      return EmployeeFactory.stream(numberIn, storeIn)
         .map {
            employeeRepository.insert(it)
         }
   }

   fun single(): Employee {
      return single(null)
   }

   fun single(storeIn: StoreEntity? = null): Employee {
      return stream(1, storeIn).findFirst().orElseThrow { Exception("Unable to create Employee") }
   }
}
