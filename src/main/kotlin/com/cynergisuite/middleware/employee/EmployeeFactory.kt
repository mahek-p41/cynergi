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


}
