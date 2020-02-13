package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.department.DepartmentFactory
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
   fun stream(
      numberIn: Int = 1,
      companyIn: Company? = null, department: DepartmentEntity? = null, store: StoreEntity? = null,
      employeeNumberIn: Int? = null,  lastNameIn: String? = null, firstNameMi: String? = null, passCodeIn: String? = null,
      activeIn: Boolean? = null, allowAutoStoreAssignIn: Boolean? = null
   ): Stream<EmployeeEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val lorem = faker.lorem()
      val numbers = faker.number()
      val name = faker.name()
      val employeeNumber = employeeNumberIn ?: numbers.numberBetween(10, 10_000)
      val company = companyIn ?: CompanyFactory.random()
      val lastName = lastNameIn ?: name.lastName()
      val passCode = passCodeIn ?: lorem.characters(3, 8)
      val active = activeIn ?: true
      val allowAutoStoreAssign = allowAutoStoreAssignIn ?: false

      return IntStream.range(0, number).mapToObj {
         EmployeeEntity(
            type = "eli",
            number = employeeNumber,
            company = company,
            lastName = lastName,
            firstNameMi = firstNameMi,
            passCode = passCode,
            store = store,
            active = active,
            allowAutoStoreAssign = allowAutoStoreAssign,
            department = department
         )
      }
   }

   @JvmStatic
   fun single(): EmployeeEntity =
      stream(1).findFirst().orElseThrow { Exception("Unable to create Employee") }

   @JvmStatic
   fun testEmployee(): EmployeeEntity {
      val store = StoreFactory.storeOneTstds1()
      val department = DepartmentFactory.forThese(store.company, "SA")

      return EmployeeEntity(
         id = 19,
         type = "sysz",
         number = 111,
         company = store.company,
         lastName = "MARTINEZ",
         firstNameMi = "DANIEL",
         passCode = "pass",
         store = store,
         active = true,
         department = department
      )
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class EmployeeFactoryService @Inject constructor(
   private val companyFactoryService: CompanyFactoryService,
   private val employeeRepository: EmployeeRepository
) {

   fun stream(numberIn: Int = 1,
      employeeNumberIn: Int? = null,
      companyIn: Company? = null,
      lastNameIn: String? = null,
      firstNameMi: String? = null,
      passCodeIn: String? = null,
      store: StoreEntity? = null,
      activeIn: Boolean? = null,
      allowAutoStoreAssignIn: Boolean? = null,
      department: DepartmentEntity? = null
   ): Stream<EmployeeEntity> {
      val company = companyIn?: store?.company ?: companyFactoryService.random()

      return EmployeeFactory.stream(
         numberIn = numberIn,
         employeeNumberIn = employeeNumberIn,
         companyIn = company,
         lastNameIn = lastNameIn,
         firstNameMi = firstNameMi,
         passCodeIn = passCodeIn,
         store = store,
         activeIn = activeIn,
         allowAutoStoreAssignIn = allowAutoStoreAssignIn,
         department = department
      ).map {
         employeeRepository.insert(it).copy(passCode = it.passCode)
      }
   }

   fun single(): EmployeeEntity =
      single(employeeNumberIn = null)

   fun single(
      employeeNumberIn: Int? = null,
      companyIn: Company? = null,
      lastNameIn: String? = null,
      firstNameMi: String? = null,
      passCodeIn: String? = null,
      store: StoreEntity? = null,
      allowAutoStoreAssignIn: Boolean? = null,
      department: DepartmentEntity? = null
   ): EmployeeEntity =
      stream(
         employeeNumberIn = employeeNumberIn,
         companyIn = companyIn,
         lastNameIn = lastNameIn,
         firstNameMi = firstNameMi,
         passCodeIn = passCodeIn,
         store = store,
         activeIn = true,
         allowAutoStoreAssignIn = allowAutoStoreAssignIn,
         department = department
      ).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }

   fun single(store: StoreEntity): EmployeeEntity =
      single(employeeNumberIn = null, store = store)
}
