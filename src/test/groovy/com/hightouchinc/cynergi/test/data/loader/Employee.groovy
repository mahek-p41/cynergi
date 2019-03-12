package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.Department
import com.hightouchinc.cynergi.middleware.entity.Employee
import com.hightouchinc.cynergi.middleware.repository.EmployeeRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class EmployeeTestDataLoader {
   static Stream<Employee> stream(int number = 1, Integer level = null, Department department = null, Company company = null) {
      final int value = number > 0 ? number : 1
      final Faker faker = new Faker()
      final num = faker.number()
      final employeeLevel = level != null && level < 100 && level > 0 ? level : num.numberBetween(1, 99)
      final Department employeeDepartment = department != null ? department : DepartmentTestDataLoader.single(employeeLevel)
      final Company employeeCompany = company != null ? company : CompanyTestDataLoader.single()
      final name = faker.name()
      final lorem = faker.lorem()

      return IntStream.range(0, value).mapToObj {
         new Employee(
            name.username(),
            lorem.characters(2, 8),
            name.firstName(),
            name.lastName(),
            employeeLevel,
            employeeDepartment,
            employeeCompany
         )
      }
   }

   static Employee single(Integer level = null, Department department = null, Company company = null) {
      return stream(1, level, department, company).findFirst().orElseThrow { new Exception("Unable to create Employee") }
   }
}

@Singleton
@CompileStatic
class EmployeeDataLoaderService {
   private final CompanyDataLoaderService companyDataLoaderService
   private final DepartmentDataLoaderService departmentDataLoaderService
   private final EmployeeRepository employeeRepository

   EmployeeDataLoaderService(
      CompanyDataLoaderService companyDataLoaderService,
      DepartmentDataLoaderService departmentDataLoaderService,
      EmployeeRepository employeeRepository
   ) {
      this.companyDataLoaderService = companyDataLoaderService
      this.departmentDataLoaderService = departmentDataLoaderService
      this.employeeRepository = employeeRepository
   }

   Stream<Employee> stream(int number = 1, Integer level = null, Department department = null, Company company = null) {
      final Department employeeDepartment = department != null ? department : departmentDataLoaderService.single()
      final Company employeeCompany = company != null ? company : companyDataLoaderService.single()

      return EmployeeTestDataLoader.stream(number, level, employeeDepartment, employeeCompany)
         .map {
            employeeRepository.insert(it)
         }
   }

   Employee single(Integer level = null, Department department = null, Company company = null) {
      return stream(1, level, department, company).findFirst().orElseThrow { new Exception("Unable to create Employee") }
   }
}
