package com.cynergisuite.middleware.employee

import com.cynergisuite.middleware.authentication.user.AuthenticatedEmployee
import com.cynergisuite.middleware.authentication.user.SecurityGroup
import com.cynergisuite.middleware.authentication.user.infrastructure.SecurityGroupRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactory
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.department.DepartmentEntity
import com.cynergisuite.middleware.location.LocationEntity
import com.cynergisuite.middleware.store.Store
import com.github.javafaker.Faker
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.IntStream
import java.util.stream.Stream

@CompileStatic
class EmployeeTestDataLoader {
   private static final AtomicInteger employeeNumberCounter = new AtomicInteger(100_000)

   static Stream<EmployeeEntity> stream(
      int numberIn = 1,
      Integer employeeNumberIn = null,
      String lastNameIn = null,
      String firstNameMiIn = null,
      String passCode = null,
      boolean activeIn = true,
      boolean cynergiSystemAdmin = false,
      CompanyEntity companyIn = null,
      DepartmentEntity departmentIn = null,
      Store storeIn = null,
      String alternativeStoreIndicatorIn = null,
      Long alternativeAreaIn = null,
      List<SecurityGroup> securityGroupIn = null
   ) {
      final number = numberIn > 0 ? numberIn : 1
      final faker = new Faker()
      final name = faker.name()
      final lorem = faker.lorem()
      final company = companyIn ?: departmentIn?.myCompany() ?: storeIn?.myCompany() ?: CompanyFactory.random()
      final secGrp = securityGroupIn ?: Collections.singletonList(SecurityGroupTestDataLoader.random())

      if (departmentIn != null && departmentIn.myCompany() != company) {
         throw new Exception("Department's Company and Company do not match ${departmentIn.myCompany()} / $company")
      }

      storeIn?.myCompany()?.with {
         if (it != company) {
            throw new Exception("Store's Company and Company do not match $it / $company")
         }
      }

      return IntStream.range(0, number).mapToObj {
         new EmployeeEntity(
            null,
            "eli",
            employeeNumberIn ?: employeeNumberCounter.incrementAndGet(),
            lastNameIn ?: name.lastName(),
            firstNameMiIn ?:name.lastName(),
            passCode ?: lorem.characters(3, 6),
            activeIn,
            cynergiSystemAdmin,
            companyIn,
            departmentIn,
            storeIn,
            alternativeStoreIndicatorIn ?: "N",
            alternativeAreaIn ?: 0,
            secGrp
         )
      }
   }

   static EmployeeEntity single(CompanyEntity companyIn) {
      final company = companyIn ?: CompanyFactory.random()
      return stream(1, null, null, null, null, true, false, company, null, null, null, null).findFirst().orElseThrow { new Exception("Unable to create EmployeeEntity") }
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class EmployeeTestDataLoaderService {
   @Inject CompanyFactoryService companyFactoryService
   @Inject EmployeeRepository employeeRepository
   @Inject SecurityGroupTestDataLoaderService securityGroupTestDataLoader
   @Inject SecurityGroupRepository securityGroupRepository

   EmployeeEntity single(CompanyEntity company) {
      return stream(company).findFirst().orElseThrow { new Exception("Unable to create EmployeeEntity") }
   }

   EmployeeEntity single(Store storeIn) {
      return stream(1, null, null, null, null, true, false, null, null, storeIn).findFirst().orElseThrow { new Exception("Unable to create EmployeeEntity") }
   }

   EmployeeEntity single(Store storeIn, String alternativeStoreIndicator) {
      return stream(1, null, null, null, null, true, false, null, null, storeIn, alternativeStoreIndicator).findFirst().orElseThrow { new Exception("Unable to create EmployeeEntity") }
   }

   EmployeeEntity single(DepartmentEntity departmentIn) {
      return stream(1, null, null, null, null, true, false, departmentIn.myCompany(), departmentIn).findFirst().orElseThrow { new Exception("Unable to create EmployeeEntity") }
   }

   EmployeeEntity single(Store storeIn, DepartmentEntity departmentIn) {
      return stream(1, null, null, null, null, true, false, null, departmentIn, storeIn).findFirst().orElseThrow { new Exception("Unable to create EmployeeEntity") }
   }

   EmployeeEntity single(int employeeNumber, CompanyEntity company, String lastName, String firstNameMiIn = null, String passCode, boolean cynergiSystemAdmin) {
      return stream(1, employeeNumber, lastName, firstNameMiIn, passCode, true, cynergiSystemAdmin, company).findFirst().orElseThrow { new Exception("Unable to create EmployeeEntity") }
   }

   EmployeeEntity single(int employeeNumber, CompanyEntity company, DepartmentEntity department, Store store, String lastName, String firstNameMiIn = null, String passCode, String alternativeStoreIndicator, long alternativeArea) {
      return stream(1, employeeNumber, lastName, firstNameMiIn, passCode, true, false, company, department, store, alternativeStoreIndicator, alternativeArea).findFirst().orElseThrow { new Exception("Unable to create EmployeeEntity") }
   }

   EmployeeEntity singleSuperUser(int employeeNumber, CompanyEntity company, String lastName, String firstNameMiIn = null, String passCode) {
      return stream(1, employeeNumber, lastName, firstNameMiIn, passCode, true, true, company, null, null, "A", 0).findFirst().orElseThrow { new Exception("Unable to create EmployeeEntity") }
   }

   AuthenticatedEmployee singleUser(Store store) {
      return stream(1, null, null, null, null, true, false, store.myCompany(), null, store)
         .map { employee ->
            new AuthenticatedEmployee(
               employee.id,
               employee.type,
               employee.number,
               employee.company,
               employee.department,
               new LocationEntity(employee.store),
               new LocationEntity(employee.store),
               new LocationEntity(employee.store),
               employee.securityGroups,
               employee.passCode,
             //  employee.cynergiSystemAdmin,
               employee.alternativeStoreIndicator,
               employee.alternativeArea
            )
         }
         .findFirst().orElseThrow { new Exception("Unable to create AuthenticatedEmployee") }
   }

   AuthenticatedEmployee singleAuthenticated(CompanyEntity company, Store store, DepartmentEntity department) {
      return streamAuthenticated(1, company, store, department).findFirst().orElseThrow { new Exception("Unable to create AuthenticatedEmployee") }
   }

   AuthenticatedEmployee singleAuthenticated(CompanyEntity company, Store store, DepartmentEntity department, String lastName, String firstNameMi) {
      return streamAuthenticated(1, company, store, department, lastName, firstNameMi).findFirst().orElseThrow { new Exception("Unable to create AuthenticatedEmployee") }
   }

   AuthenticatedEmployee singleAuthenticated(CompanyEntity company, Store store, DepartmentEntity department, String alternativeStoreIndicator, long alternativeArea) {
      return streamAuthenticated(1, company, store, department, null, null,false, alternativeStoreIndicator, alternativeArea).findFirst().orElseThrow { new Exception("Unable to create AuthenticatedEmployee") }
   }

   private Stream<AuthenticatedEmployee> streamAuthenticated(int numberIn = 1, CompanyEntity company, Store store, DepartmentEntity department = null, String lastNameIn = null, String firstNameMiIn = null, boolean cynergiSystemAdmin = false, String alternativeStoreIndicatorIn = null, Long alternativeAreaIn = null) {
      return stream(numberIn, null, lastNameIn, firstNameMiIn, null, true, cynergiSystemAdmin, company, department, store, alternativeStoreIndicatorIn, alternativeAreaIn)
         .map { employee ->
            new AuthenticatedEmployee(
               employee.id,
               employee.type,
               employee.number,
               employee.company,
               employee.department,
               new LocationEntity(employee.store),
               new LocationEntity(employee.store),
               new LocationEntity(store),
               employee.securityGroups,
               employee.passCode,
               //employee.cynergiSystemAdmin,
               employee.alternativeStoreIndicator,
               employee.alternativeArea,
            )
         }
   }

   Stream<EmployeeEntity> stream(
      int numberIn = 1,
      Integer employeeNumberIn = null,
      String lastNameIn = null,
      String firstNameMiIn = null,
      String passCodeIn = null,
      boolean activeIn = true,
      boolean cynergiSystemAdminIn = false,
      CompanyEntity companyIn = null,
      DepartmentEntity departmentIn = null,
      Store storeIn = null,
      String alternativeStoreIndicator = null,
      Long alternativeArea = null,
      SecurityGroup securityGroupIn = null
   ) {
      final company = companyIn ?: departmentIn?.myCompany() ?: storeIn?.myCompany() ?: companyFactoryService.random()
      SecurityGroup securityGroup = (cynergiSystemAdminIn) ?
         (securityGroupRepository.findByName(company, "HTADMIN") ?: securityGroupTestDataLoader.single(company, "HTADMIN"))
         : (securityGroupRepository.findByName(company, "basic") ?: securityGroupTestDataLoader.single(company, "basic"))
      List<SecurityGroup> securityGroupList = Collections.singletonList(securityGroup)

      return EmployeeTestDataLoader.stream(numberIn, employeeNumberIn, lastNameIn, firstNameMiIn, passCodeIn, activeIn, cynergiSystemAdminIn, company, departmentIn, storeIn, alternativeStoreIndicator, alternativeArea, securityGroupList)
         .map {
            employeeRepository.insert(it).copyMeWithDifferentPassCode(it.passCode)
            }.peek(emp -> {
               emp.securityGroups.forEach {
                  securityGroupTestDataLoader.assignEmployeeToSecurityGroup(emp, it)
               }
      })
   }

   Stream<EmployeeEntity> stream(int numberIn = 1, CompanyEntity company) {
      return stream(numberIn, null, null, null, null, true, false, company, null, null, null, null)
   }
}
