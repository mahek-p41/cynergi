package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactoryService
import com.cynergisuite.middleware.division.DivisionEntity
import com.cynergisuite.middleware.division.DivisionFactoryService
import com.cynergisuite.middleware.region.RegionEntity
import com.cynergisuite.middleware.region.RegionFactoryService
import com.cynergisuite.middleware.store.StoreFactoryService
import spock.lang.Specification

import javax.inject.Inject

abstract class ServiceSpecificationBase extends Specification {
   @Inject CompanyFactoryService companyFactoryService
   @Inject DivisionFactoryService divisionFactoryService
   @Inject RegionFactoryService regionFactoryService
   @Inject StoreFactoryService storeFactoryService
   @Inject TruncateDatabaseService truncateDatabaseService

   List<CompanyEntity> companies
   List<DivisionEntity> divisions
   List<RegionEntity> regions

   void setup() {
      truncateDatabaseService.truncate()
      this.companies = companyFactoryService.streamPredefined().toList() // create the default companies
      this.divisions = companies.collect { company ->  divisionFactoryService.single(company) }.toList()
      this.regions = divisions.collect { division -> regionFactoryService.single(division) }.toList()
      this.regions.collect { region -> storeFactoryService.companyStoresToRegion(region.division.company, region).toList() }
   }
}
