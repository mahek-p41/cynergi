package com.hightouchinc.cynergi.test.data.loader

import com.github.javafaker.Faker
import com.hightouchinc.cynergi.middleware.entity.Company
import com.hightouchinc.cynergi.middleware.entity.CompanyModuleAccess
import com.hightouchinc.cynergi.middleware.entity.Module
import com.hightouchinc.cynergi.middleware.repository.CompanyModuleAccessRepository
import groovy.transform.CompileStatic

import javax.inject.Singleton
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class CompanyModuleAccessTestDataLoader {
   static Stream<CompanyModuleAccess> stream(int number = 1, Integer levelIn = null, Company companyIn = null, Module moduleIn = null) {
      final int value = number > 0 ? number : 1
      final Faker faker = new Faker()
      final num = faker.number()
      final level = levelIn != null && levelIn < 100 && levelIn > 0 ? levelIn : num.numberBetween(1, 99)
      final Company company = companyIn ?: CompanyTestDataLoader.single(null)
      final Module module = moduleIn?: ModuleTestDataLoader.single(null)

      return IntStream.range(0, value).mapToObj {
         new CompanyModuleAccess(
            level,
            company,
            module
         )
      }
   }

   static CompanyModuleAccess single() {
      return stream(1).findFirst().orElseThrow { new Exception("Unable to create CompanyModuleAccess") }
   }
}

@Singleton
@CompileStatic
class CompanyModuleAccessDataLoaderService {
   private final CompanyDataLoaderService companyDataLoaderService
   private final CompanyModuleAccessRepository companyModuleAccessRepository
   private final ModuleDataLoaderService moduleDataLoaderService

   CompanyModuleAccessDataLoaderService(
      CompanyDataLoaderService companyDataLoaderService,
      CompanyModuleAccessRepository companyModuleAccessRepository,
      ModuleDataLoaderService moduleDataLoaderService
   ) {
      this.companyDataLoaderService = companyDataLoaderService
      this.companyModuleAccessRepository = companyModuleAccessRepository
      this.moduleDataLoaderService = moduleDataLoaderService
   }

   Stream<CompanyModuleAccess> stream(int number = 1, Integer levelIn = null, Company companyIn = null, Module moduleIn = null) {
      final Company company = companyIn != null ? companyIn : companyDataLoaderService.single(null)
      final Module module = moduleIn != null ? moduleIn : moduleDataLoaderService.single()

      return CompanyModuleAccessTestDataLoader.stream(number, levelIn, company, module)
         .map {
            companyModuleAccessRepository.insert(it)
         }
   }

   List<CompanyModuleAccess> associate(List<Module> modulesIn = null, Company companyIn = null, Integer levelIn = null) {
      final Integer level = levelIn != null && levelIn > 0 && levelIn < 99 ? levelIn : 1
      final Company company = companyIn ?: companyDataLoaderService.single(null)
      final List<Module> modules = modulesIn != null && modulesIn.size() > 0 ? modulesIn : [moduleDataLoaderService.single()]

      return modules
         .collect {
            new CompanyModuleAccess(
               level,
               company,
               it
            )
         }.collect {
            companyModuleAccessRepository.insert(it)
         }
   }

   CompanyModuleAccess single(Integer level = null, Company company = null, Module module = null) {
      return stream(1, level, company, module).findFirst().orElseThrow { new Exception("Unable to create CompanyModuleAccess") }
   }
}
