package com.cynergisuite.middleware.accounting.general.ledger.reconciliation

import com.cynergisuite.domain.GeneralLedgerReconciliationReportFilterRequest
import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.accounting.general.ledger.reconciliation.infrastructure.GeneralLedgerReconciliationRepository
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.inventory.infrastructure.InventoryEomRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Locale

@Singleton
class GeneralLedgerReconciliationService @Inject constructor(
   private val glReconciliationRepository: GeneralLedgerReconciliationRepository,
){

   fun fetchReport(company: CompanyEntity, filterRequest: GeneralLedgerReconciliationReportFilterRequest, locale: Locale): GeneralLedgerReconciliationReportEntity {
      return glReconciliationRepository.fetchReport(filterRequest, company)
   }

   private fun transformEntity(entity: GeneralLedgerReconciliationReportEntity): GeneralLedgerReconciliationReportDTO {
      return GeneralLedgerReconciliationReportDTO(entity)
   }
}
