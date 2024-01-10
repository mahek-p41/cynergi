package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.area.AccountPayable
import com.cynergisuite.middleware.area.AreaDataTestDataLoaderService
import com.cynergisuite.middleware.area.BankReconciliation
import com.cynergisuite.middleware.area.PurchaseOrder
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.department.DepartmentFactoryService
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.division.DivisionFactoryService
import com.cynergisuite.middleware.employee.EmployeeTestDataLoaderService
import com.cynergisuite.middleware.region.RegionEntity
import com.cynergisuite.middleware.region.RegionTestDataLoaderService
import com.cynergisuite.middleware.store.StoreTestDataLoaderService
import jakarta.inject.Inject
import spock.lang.Specification

abstract class ServiceSpecificationBase extends Specification {
   @Inject CompanyFactoryService companyFactoryService
   @Inject DepartmentFactoryService departmentFactoryService
   @Inject DivisionFactoryService divisionFactoryService
   @Inject RegionTestDataLoaderService regionFactoryService
   @Inject StoreTestDataLoaderService storeFactoryService
   @Inject EmployeeTestDataLoaderService employeeFactoryService
   @Inject TruncateDatabaseService truncateDatabaseService
   @Inject AreaDataTestDataLoaderService areaDataLoaderService

   List<CompanyEntity> companies
   List<DivisionEntity> divisions
   List<RegionEntity> regions

   Boolean inventoryLoaded = false

   void setup() {
      truncateDatabaseService.truncate() //need to exclude the inventory table for test
      if(!inventoryLoaded) {
         truncateDatabaseService.loadInventory()
         inventoryLoaded = true
      }
      this.companies = companyFactoryService.streamPredefined().toList() // create the default companies

      def tstds1 = companies.find {
         it.datasetCode == "coravt"
      }
      def tstds1DivisionalManagerDepartment = departmentFactoryService.forThese(tstds1, "SM")
      def tstds1Store1DivisionalManager = employeeFactoryService.single(tstds1DivisionalManagerDepartment)
      def division1 = divisionFactoryService.single(tstds1, tstds1Store1DivisionalManager)

      def tstds2 = companies.find { it.datasetCode == "corrto" }
      def division2 = divisionFactoryService.single(tstds2)

      divisions = [division1, division2]

      this.regions = divisions.collect { division -> regionFactoryService.single(division) }.toList()
      // Assign region for maximum 2 stores of each company
      this.regions.each { region ->
         storeFactoryService.companyStoresToRegion(region, 1).toList()
      }

      areaDataLoaderService.enableArea(AccountPayable.INSTANCE, tstds1)
      areaDataLoaderService.enableArea(BankReconciliation.INSTANCE, tstds1)
      areaDataLoaderService.enableArea(PurchaseOrder.INSTANCE, tstds1)
      areaDataLoaderService.enableArea(AccountPayable.INSTANCE, tstds2)
      areaDataLoaderService.enableArea(BankReconciliation.INSTANCE, tstds2)
   }
}
