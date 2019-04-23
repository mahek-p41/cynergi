package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.Department
import com.hightouchinc.cynergi.middleware.repository.DepartmentRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class DepartmentTestDataLoader {
   static Stream<Department> stream(int numberIn = 1, Integer levelIn = null, Company companyIn = null) {
      final int number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final comp = faker.company()
      final num = faker.number()
      final int departmentLevel = levelIn ?: num.numberBetween(1, 99)
      final Company company = companyIn ?: CompanyTestDataLoader.single(null)

      return IntStream.range(0, number).mapToObj {
         new Department(
            comp.name(),
            departmentLevel,
            company
         )
      }
   }

   static Department single(Integer level = null) {
      return stream(1, level).findFirst().orElseThrow { new Exception("Unable to create Department") }
   }
}

@Singleton
@CompileStatic
class DepartmentDataLoaderService {
   private final CompanyDataLoaderService companyDataLoaderService
   private final DepartmentRepository departmentRepository

   DepartmentDataLoaderService(
      CompanyDataLoaderService companyDataLoaderService,
      DepartmentRepository departmentRepository
   ) {
      this.companyDataLoaderService = companyDataLoaderService
      this.departmentRepository = departmentRepository
   }

   Stream<Department> stream(int number = 1, Integer level = null, Company companyIn = null) {
      final Company company = companyIn ?: companyDataLoaderService.single(null)

      return DepartmentTestDataLoader.stream(number, level, company)
         .map {
            departmentRepository.insert(it)
         }
   }

   Department single(Integer level = null) {
      return stream(1, level).findFirst().orElseThrow { new Exception("Unable to create Department") }
   }
}
