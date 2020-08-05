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
         description = "ACCOUNT PAYABLE",
         localizationCode = "account.payable.area.and.functionality"
      ),
      AreaType(
         id = 2,
         value = "BR",
         description = "BANK RECONCILIATION",
         localizationCode = "bank.reconciliation.area.and.functionality"
      ),
      AreaType(
         id = 3,
         value = "GL",
         description = "GENERAL LEDGER",
         localizationCode = "general.ledger.area.and.functionality"
      ),
      AreaType(
         id = 4,
         value = "PO",
         description = "PURCHASE ORDER",
         localizationCode = "purchase.order.and.requisition.area.and.functionality"
      ),
      AreaType(
         id = 5,
         value = "MCF",
         description = "MASTER CONTROL FILES",
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

   fun enableArea(id: Long, company: Company) =
      repository.insert(company, predefined().first { it.id == id })
}
