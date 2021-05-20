package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.CompanyEntity
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires

import javax.inject.Singleton

class AreaDataLoader {
   private static final List<AreaType> areaTypes = [
      new AreaType(
         1,
         "AP",
         "Account Payable",
         "account.payable.area.and.functionality",
         false,
         []
      ),
      new AreaType(
         2,
         "BR",
         "Bank Reconciliation",
         "bank.reconciliation.area.and.functionality",
         false,
         []
      ),
      new AreaType(
         3,
         "GL",
         "General Ledger",
         "general.ledger.area.and.functionality",
         false,
         []
      ),
      new AreaType(
         4,
         "PO",
         "Purchase Order",
         "purchase.order.and.requisition.area.and.functionality",
         false,
         []
      ),
      new AreaType(
         5,
         "MCF",
         "Master Control Files",
         "master.control.files",
         false,
         []
      )
   ]

   static List<AreaType> areaTypes() { areaTypes }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class AreaDataLoaderService {
   private final AreaRepository repository

   AreaDataLoaderService(AreaRepository repository) {
      this.repository = repository
   }

   def enableArea(int id, CompanyEntity company) {
      repository.insert(company, AreaDataLoader.areaTypes().find { it.id == id })
   }
}
