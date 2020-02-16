package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.department.DepartmentFactory
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.StoreEntity
import com.cynergisuite.middleware.store.StoreFactory
import com.github.javafaker.Faker
import io.micronaut.context.annotation.Requires
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Singleton

object EmployeeFactory {

   @JvmStatic
   private val employeeNumberCounter = AtomicInteger(100_000)

   @JvmStatic
   fun stream(numberIn: Int = 1, employeeNumberIn: Int? = null, lastNameIn: String? = null, firstNameMiIn: String? = null, passCode: String? = null, activeIn: Boolean = true, cynergiSystemAdminIn: Boolean = false, companyIn: Company? = null, departmentIn: Department? = null, storeIn: StoreEntity? = null): Stream<EmployeeEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
      val random = faker.random()
      val name = faker.name()
      val lorem = faker.lorem()
      val company = companyIn ?: departmentIn?.myCompany() ?: storeIn?.company ?: CompanyFactory.random()

      if (departmentIn != null && departmentIn.myCompany() != company) {
         throw Exception("Department's Company and Company do not match ${departmentIn.myCompany()} / $company")
      }

      if (storeIn != null && storeIn.myCompany() != company) {
         throw Exception("Store's Company and Company do not match ${storeIn.myCompany()} / $company")
      }

      return IntStream.range(0, number).mapToObj {
         EmployeeEntity(
            type = "eli",
            number = employeeNumberIn ?: employeeNumberCounter.incrementAndGet(),
            lastName = lastNameIn ?: name.lastName(),
            firstNameMi = firstNameMiIn ?: name.firstName(),
            passCode = passCode ?: lorem.characters(1, 6),
            active = activeIn,
            cynergiSystemAdmin = cynergiSystemAdminIn,
            company = company,
            department = departmentIn,
            store = storeIn
         )
      }
   }

   @JvmStatic
   fun single(companyIn: Company?): EmployeeEntity {
      val company = companyIn ?: CompanyFactory.random()

      return stream(companyIn = company).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }
   }

   @JvmStatic
   fun testEmployee(companyIn: Company?): EmployeeEntity {
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

   fun stream(numberIn: Int = 1, employeeNumberIn: Int? = null, lastNameIn: String? = null, firstNameMiIn: String? = null, passCode: String? = null, activeIn: Boolean = true, cynergiSystemAdminIn: Boolean = false, companyIn: Company? = null, departmentIn: Department? = null, storeIn: StoreEntity? = null): Stream<EmployeeEntity> {
      val company = companyIn ?: departmentIn?.myCompany() ?: storeIn?.company ?: companyFactoryService.random()

      return EmployeeFactory.stream(numberIn, employeeNumberIn, lastNameIn, firstNameMiIn, passCode, activeIn, cynergiSystemAdminIn, company, departmentIn, storeIn)
         .map { employeeRepository.insert(it).copy(passCode = it.passCode) }
   }

   fun single(storeIn: StoreEntity?): EmployeeEntity {
      return stream(storeIn = storeIn).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }
   }

   fun single(storeIn: StoreEntity?, departmentIn: Department?): EmployeeEntity {
      return stream(storeIn = storeIn, departmentIn = departmentIn).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }
   }

   fun single(employeeNumberIn: Int?, companyIn: Company?, lastNameIn: String?, firstNameMiIn: String?, passCode: String?, cynergiSystemAdminIn: Boolean): EmployeeEntity {
      return stream(employeeNumberIn = employeeNumberIn, companyIn = companyIn, lastNameIn = lastNameIn, firstNameMiIn = firstNameMiIn, passCode = passCode, cynergiSystemAdminIn = cynergiSystemAdminIn).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }
   }
}
