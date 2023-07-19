package com.cynergisuite.middleware.accounting.general.ledger.deposit

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.general.ledger.deposit.infrastructure.StagingDepositRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class StagingDepositService @Inject constructor(
   private val stagingDepositRepository: StagingDepositRepository
) {
   fun fetchAll(company: CompanyEntity, pageRequest: StagingDepositPageRequest): Page<StagingDepositDTO> {
      val found = stagingDepositRepository.findAll(company, pageRequest)

      return found.toPage { entity: StagingDepositEntity ->
         StagingDepositDTO(entity)
      }
   }

   fun fetchAccountingDetails(company: CompanyEntity, verifyId: UUID): AccountingDetailWrapper {
      val found = stagingDepositRepository.fetchAccountingDetails(company, verifyId)
      return AccountingDetailWrapper(found)
   }
}
