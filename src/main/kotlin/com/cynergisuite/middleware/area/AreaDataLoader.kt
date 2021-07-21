package com.cynergisuite.middleware.area

import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.Company
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

object AreaDataLoader {
   private val areaTypes = listOf(
      AreaType(
         id = 1,
         value = "AP",
         description = "Account Payable",
         localizationCode = "account.payable.area.and.functionality"
      ),
      AreaType(
         id = 2,
         value = "BR",
         description = "Bank Reconciliation",
         localizationCode = "bank.reconciliation.area.and.functionality"
      ),
      AreaType(
         id = 3,
         value = "GL",
         description = "General Ledger",
         localizationCode = "general.ledger.area.and.functionality"
      ),
      AreaType(
         id = 4,
         value = "PO",
         description = "Purchase Order",
         localizationCode = "purchase.order.and.requisition.area.and.functionality"
      ),
      AreaType(
         id = 5,
         value = "MCF",
         description = "Master Control Files",
         localizationCode = "master.control.files"
      )
   )

   @JvmStatic
   fun areaTypes(): List<AreaType> = areaTypes
}

@Singleton
@Requires(env = ["develop", "test"])
class AreaDataLoaderService(
   private val repository: AreaRepository
) {
   fun predefined() = AreaDataLoader.areaTypes()

   fun enableArea(id: Int, company: Company) =
      repository.insert(company, predefined().first { it.id == id })
}
