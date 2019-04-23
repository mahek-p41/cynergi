package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Department
import com.hightouchinc.cynergi.middleware.entity.Employee
import com.hightouchinc.cynergi.middleware.repository.EmployeeRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class EmployeeTestDataLoader {
   static Stream<Employee> stream(int number = 1, Department department = null) {
      final int value = number > 0 ? number : 1
      final Faker faker = new Faker()
      final Department employeeDepartment = department ?: DepartmentTestDataLoader.single()
      final name = faker.name()
      final lorem = faker.lorem()

      return IntStream.range(0, value).mapToObj {
         new Employee(
            lorem.characters(1, 8),
            lorem.characters(2, 8),
            name.firstName(),
            name.lastName(),
            employeeDepartment
         )
      }
   }

   static Employee single(Department department = null) {
      return stream(1, department).findFirst().orElseThrow { new Exception("Unable to create Employee") }
   }
}

@Singleton
@CompileStatic
class EmployeeDataLoaderService {
   private final DepartmentDataLoaderService departmentDataLoaderService
   private final EmployeeRepository employeeRepository

   EmployeeDataLoaderService(
      DepartmentDataLoaderService departmentDataLoaderService,
      EmployeeRepository employeeRepository
   ) {
      this.departmentDataLoaderService = departmentDataLoaderService
      this.employeeRepository = employeeRepository
   }

   Stream<Employee> stream(int number = 1, Department department = null) {
      final Department employeeDepartment = department != null ? department : departmentDataLoaderService.single()

      return EmployeeTestDataLoader.stream(number, employeeDepartment)
         .map {
            employeeRepository.insert(it)
         }
   }

   Employee single(Department department = null) {
      return stream(1, department).findFirst().orElseThrow { new Exception("Unable to create Employee") }
   }
}
