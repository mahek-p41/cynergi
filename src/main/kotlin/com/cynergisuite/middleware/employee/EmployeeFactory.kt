package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.department.Department
import com.cynergisuite.middleware.employee.infrastructure.EmployeeRepository
import com.cynergisuite.middleware.store.StoreEntity
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
   fun stream(numberIn: Int = 1, employeeNumberIn: Int? = null, lastNameIn: String? = null, firstNameMiIn: String? = null, passCode: String? = null, activeIn: Boolean = true, cynergiSystemAdminIn: Boolean = false, companyIn: Company? = null, departmentIn: Department? = null, storeIn: StoreEntity? = null, alternativeStoreIndicatorIn: String? = null, alternativeAreaIn: Int? = null): Stream<EmployeeEntity> {
      val number = if (numberIn > 0) numberIn else 1
      val faker = Faker()
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
            passCode = passCode ?: lorem.characters(3, 6),
            active = activeIn,
            cynergiSystemAdmin = cynergiSystemAdminIn,
            company = company,
            department = departmentIn,
            store = storeIn,
            alternativeStoreIndicator = alternativeStoreIndicatorIn ?: "A",
            alternativeArea = alternativeAreaIn ?: 0
         )
      }
   }

   @JvmStatic
   fun single(companyIn: Company?): EmployeeEntity {
      val company = companyIn ?: CompanyFactory.random()

      return stream(companyIn = company).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }
   }
}

@Singleton
@Requires(env = ["develop", "test"])
class EmployeeFactoryService @Inject constructor(
   private val companyFactoryService: CompanyFactoryService,
   private val employeeRepository: EmployeeRepository
) {

   fun single(company: Company): EmployeeEntity {
      return stream(companyIn = company).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }
   }

   fun single(storeIn: StoreEntity): EmployeeEntity {
      return stream(storeIn = storeIn).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }
   }

   fun single(storeIn: StoreEntity, departmentIn: Department): EmployeeEntity {
      return stream(storeIn = storeIn, departmentIn = departmentIn).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }
   }

   fun single(employeeNumber: Int, company: Company, lastName: String, firstNameMiIn: String? = null, passCode: String, cynergiSystemAdmin: Boolean): EmployeeEntity {
      return stream(employeeNumberIn = employeeNumber, companyIn = company, lastNameIn = lastName, firstNameMiIn = firstNameMiIn, passCodeIn = passCode, cynergiSystemAdminIn = cynergiSystemAdmin).findFirst().orElseThrow { Exception("Unable to create EmployeeEntity") }
   }

   fun singleUser(store: StoreEntity): AuthenticatedEmployee {
      return stream(companyIn = store.company, storeIn = store)
         .map { employee ->
            AuthenticatedEmployee(
               id = employee.id!!,
               type = employee.type,
               number = employee.number,
               company = employee.company,
               department = employee.department,
               location = employee.store,
               fallbackLocation = store,
               passCode = employee.passCode,
               cynergiSystemAdmin = employee.cynergiSystemAdmin,
               altStoreIndicator = employee.alternativeStoreIndicator
            )
         }
         .findFirst().orElseThrow { Exception("Unable to create AuthenticatedEmployee") }
   }

   fun singleAuthenticated(company: Company, store: StoreEntity, department: Department): AuthenticatedEmployee {
      return streamAuthenticated(company = company, store = store, department = department).findFirst().orElseThrow { Exception("Unable to create AuthenticatedEmployee") }
   }

   fun singleAuthenticated(company: Company, store: StoreEntity, department: Department, lastName: String, firstNameMi: String): AuthenticatedEmployee {
      return streamAuthenticated(company = company, store = store, department = department, lastNameIn = lastName, firstNameMiIn = firstNameMi).findFirst().orElseThrow { Exception("Unable to create AuthenticatedEmployee") }
   }

   private fun streamAuthenticated(numberIn: Int = 1, company: Company, store: StoreEntity, department: Department? = null, lastNameIn: String? = null, firstNameMiIn: String? = null): Stream<AuthenticatedEmployee> {
      return stream(numberIn = numberIn, storeIn = store, companyIn = company, departmentIn = department, lastNameIn = lastNameIn, firstNameMiIn = firstNameMiIn)
         .map { employee ->
            AuthenticatedEmployee(
               id = employee.id!!,
               type = employee.type,
               number = employee.number,
               company = employee.company,
               department = employee.department,
               location = employee.store,
               fallbackLocation = store,
               passCode = employee.passCode,
               cynergiSystemAdmin = employee.cynergiSystemAdmin,
               altStoreIndicator = employee.alternativeStoreIndicator
            )
         }
   }

   private fun stream(numberIn: Int = 1, employeeNumberIn: Int? = null, lastNameIn: String? = null, firstNameMiIn: String? = null, passCodeIn: String? = null, activeIn: Boolean = true, cynergiSystemAdminIn: Boolean = false, companyIn: Company? = null, departmentIn: Department? = null, storeIn: StoreEntity? = null): Stream<EmployeeEntity> {
      val company = companyIn ?: departmentIn?.myCompany() ?: storeIn?.company ?: companyFactoryService.random()

      return EmployeeFactory.stream(numberIn, employeeNumberIn, lastNameIn, firstNameMiIn, passCodeIn, activeIn, cynergiSystemAdminIn, company, departmentIn, storeIn)
         .map { employeeRepository.insert(it).copy(passCode = it.passCode) }
   }
}
