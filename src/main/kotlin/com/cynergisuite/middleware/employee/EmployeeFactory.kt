package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object EmployeeFactory {

   @JvmStatic
   fun stream(numberIn: Int = 1): Stream<Employee> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val bool = faker.bool()
      val numbers = faker.number()

      return IntStream.range(0, number).mapToObj {
         Employee(
            loc = "int",
            number = numbers.numberBetween(1, 10_000),
            passCode = lorem.characters(3, 6),
            active = bool.bool()
         )
      }
   }

   @JvmStatic
   fun single(): Employee {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create Employee") }
   }
}

@Singleton
@Requires(env = ["demo", "test"])
class EmployeeFactoryService @Inject constructor(
   private val employeeRepository: EmployeeRepository
) {

   fun stream(numberIn: Int = 1): Stream<Employee> {
      return EmployeeFactory.stream(numberIn)
         .map {
            employeeRepository.insert(it)
         }
   }

   fun single(): Employee {
      return stream(1).findFirst().orElseThrow { Exception("Unable to create Employee") }
   }
}
