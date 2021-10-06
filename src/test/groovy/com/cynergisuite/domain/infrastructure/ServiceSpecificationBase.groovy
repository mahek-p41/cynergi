package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.division.DivisionFactoryService
import com.cynergisuite.middleware.employee.EmployeeTestDataLoaderService
import com.cynergisuite.middleware.region.RegionEntity
import com.cynergisuite.middleware.region.RegionTestDataLoaderService
import com.cynergisuite.middleware.store.StoreTestDataLoaderService
import spock.lang.Specification

import javax.inject.Inject

abstract class ServiceSpecificationBase extends Specification {
   @Inject CompanyFactoryService companyFactoryService
   @Inject DepartmentFactoryService departmentFactoryService
   @Inject DivisionFactoryService divisionFactoryService
   @Inject RegionTestDataLoaderService regionFactoryService
   @Inject StoreTestDataLoaderService storeFactoryService
   @Inject EmployeeTestDataLoaderService employeeFactoryService
   @Inject TruncateDatabaseService truncateDatabaseService

   List<CompanyEntity> companies
   List<DivisionEntity> divisions
   List<RegionEntity> regions

   void setup() {

      truncateDatabaseService.truncate()
      this.companies = companyFactoryService.streamPredefined().toList() // create the default companies

      def tstds1 = companies.find {
         it.datasetCode == "tstds1"
      }
      def tstds1DivisionalManagerDepartment = departmentFactoryService.forThese(tstds1, "EX")
      def tstds1Store1DivisionalManager = employeeFactoryService.single(tstds1DivisionalManagerDepartment)
      def division1 = divisionFactoryService.single(tstds1)

      def tstds2 = companies.find { it.datasetCode == "tstds2" }
      def division2 = divisionFactoryService.single(tstds2)

      divisions = [division1, division2]

      this.regions = divisions.collect { division -> regionFactoryService.single(division) }.toList()
      // Assign region for maximum 2 stores of each company
      this.regions.each { region ->
         storeFactoryService.companyStoresToRegion(region, 2).toList()
      }
   }
}
