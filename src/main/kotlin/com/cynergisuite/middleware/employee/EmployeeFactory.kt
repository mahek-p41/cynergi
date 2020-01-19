package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactory
import com.cynergisuite.middleware.store.StoreFactoryService
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object EmployeeFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1, employeeNumberIn: Int? = null, firstNameMiIn: String? = null, lastNameIn: String? = null, storeIn: StoreEntity? = null, passCodeIn: String? = null, activeIn: Boolean = true, allowAutoStoreAssignIn: Boolean = false): Stream<EmployeeEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val numbers = faker.number()
      val name = faker.name()
      val employeeNumber = employeeNumberIn ?: numbers.numberBetween(1, 10_000)
      val store = storeIn ?: StoreFactory.random()
      val passCode = passCodeIn ?: lorem.characters(5, 8)
      val lastName = lastNameIn ?: name.lastName()

      return IntStream.range(0, number).mapToObj {
         EmployeeEntity(
            type = "eli",
            number = employeeNumber,
            dataset = store.dataset,
            lastName = lastName,
            firstNameMi = firstNameMiIn,
            passCode = passCode,
            store = store,
            active = activeIn,
            allowAutoStoreAssign = allowAutoStoreAssignIn
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
   private val employeeRepository: EmployeeRepository,
   private val storeFactoryService: StoreFactoryService
) {

   fun stream(numberIn: Int = 1, employeeNumberIn: Int? = null, firstNameMiIn: String? = null, lastNameIn: String? = null, storeIn: StoreEntity? = null, passCodeIn: String? = null, activeIn: Boolean = true, allowAutoStoreAssignIn: Boolean = false): Stream<EmployeeEntity> {
      val store = storeIn ?: storeFactoryService.random()

      return EmployeeFactory.stream(numberIn = numberIn, employeeNumberIn = employeeNumberIn, firstNameMiIn = firstNameMiIn, lastNameIn = lastNameIn, storeIn = store, passCodeIn = passCodeIn, activeIn = activeIn, allowAutoStoreAssignIn = allowAutoStoreAssignIn)
         .map {
            employeeRepository.insert(it)
         }
   }

   fun single(): EmployeeEntity =
      single(employeeNumberIn = null, firstNameMiIn = null, lastNameIn = null, storeIn = null, passCodeIn = null)

   fun single(storeIn: StoreEntity? = null): EmployeeEntity =
      single(storeIn = storeIn, firstNameMiIn = null)

   fun single(employeeNumberIn: Int? = null, firstNameMiIn: String? = null, lastNameIn: String? = null, storeIn: StoreEntity? = null, passCodeIn: String? = null, activeIn: Boolean = true, allowAutoStoreAssignIn: Boolean = false): EmployeeEntity =
      stream(employeeNumberIn = employeeNumberIn, firstNameMiIn = firstNameMiIn, lastNameIn = lastNameIn, storeIn = storeIn, passCodeIn = passCodeIn, activeIn = activeIn, allowAutoStoreAssignIn = allowAutoStoreAssignIn).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }
}
