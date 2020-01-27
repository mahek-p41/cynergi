package com.cynergisuite.domain.infrastructure

import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.company.CompanyFactoryService
import spock.lang.Specification

import javax.inject.Inject

abstract class ServiceSpecificationBase extends Specification {
   @Inject CompanyFactoryService companyFactoryService
   @Inject TruncateDatabaseService truncateDatabaseService

   List<CompanyEntity> companies

   void setup() {
      this.companies = companyFactoryService.streamPredefined().toList() // create the default companies
      truncateDatabaseService.truncate()
   }
}
