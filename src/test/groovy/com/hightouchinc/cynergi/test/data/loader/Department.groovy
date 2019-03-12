package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Department
import com.hightouchinc.cynergi.middleware.repository.DepartmentRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class DepartmentTestDataLoader {
   static Stream<Department> stream(int number = 1, Integer level = null) {
      final int value = number > 0 ? number : 1
      final faker = new Faker()
      final company = faker.company()
      final num = faker.number()
      final int departmentLevel = level ?: num.numberBetween(1, 99)

      return IntStream.range(0, value).mapToObj {
         new Department(
            company.name(),
            departmentLevel
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
   private final DepartmentRepository departmentRepository

   DepartmentDataLoaderService(DepartmentRepository departmentRepository) {
      this.departmentRepository = departmentRepository
   }

   Stream<Department> stream(int number = 1, Integer level = null) {
      return DepartmentTestDataLoader.stream(number, level)
         .map {
            departmentRepository.insert(it)
         }
   }

   Department single(Integer level = null) {
      return stream(1, level).findFirst().orElseThrow { new Exception("Unable to create Department") }
   }
}
