package com.cynergisuite.middleware.area

import com.cynergisuite.extensions.forId
import com.cynergisuite.middleware.area.infrastructure.AreaRepository
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.company.CompanyFactory
import io.micronaut.context.annotation.Requires
import java.util.stream.Stream
import javax.inject.Singleton

object AreaDataLoader {
   private val areaTypes = listOf(
      AreaType(
         id = 1,
         value = "AP",
         description = "ACCOUNT PAYABLE",
         localizationCode = "account.payable.areaEntity.and.functionality"
      ),
      AreaType(
         id = 2,
         value = "BR",
         description = "BANK RECONCILIATION",
         localizationCode = "bank.reconciliation.areaEntity.and.functionality"
      ),
      AreaType(
         id = 3,
         value = "GL",
         description = "GENERAL LEDGER",
         localizationCode = "general.ledger.areaEntity.and.functionality"
      ),
      AreaType(
         id = 4,
         value = "PO",
         description = "PURCHASE ORDER",
         localizationCode = "purchase.order.and.requisition.areaEntity.and.functionality"
      ),
      AreaType(
         id = 5,
         value = "MCF",
         description = "MASTER CONTROL FILES",
         localizationCode = "master.control.files"
      )
   )

   private val areaConfigEntities = listOf(
      AreaEntity(
         company = CompanyFactory.tstds1(),
         areaType = areaTypes.forId(1)!!
      ),
      AreaEntity(
         company = CompanyFactory.tstds1(),
         areaType = areaTypes.forId(2)!!
      ),
      AreaEntity(
         company = CompanyFactory.tstds1(),
         areaType = areaTypes.forId(4)!!
      ),
      AreaEntity(
         company = CompanyFactory.tstds2(),
         areaType = areaTypes.forId(1)!!
      ),
      AreaEntity(
         company = CompanyFactory.tstds2(),
         areaType = areaTypes.forId(2)!!
      )
   )

   @JvmStatic
   fun areaTypes(): List<AreaType> = areaTypes

   @JvmStatic
   fun predefinedConfigEntities(): List<AreaEntity> = areaConfigEntities

}

@Singleton
@Requires(env = ["develop", "test"])
class AreaDataLoaderService(
   private val repository: AreaRepository
) {
   fun predefined() = AreaDataLoader.areaTypes()
   fun predefinedConfigEntities() = AreaDataLoader.predefinedConfigEntities()
   fun areaConfigs(company: Company): Stream<AreaEntity> = AreaDataLoader.predefinedConfigEntities()
      .stream()
      .filter {
         it.company.myDataset() == company.myDataset()
      }
      .map {
         repository.insert(company, it)
      }

   fun enableArea(areaTypeId: Long, company: Company) =
      repository.insert(areaEntity = AreaEntity(areaType = predefined().forId(areaTypeId)!!, company = company), company = company)
}
