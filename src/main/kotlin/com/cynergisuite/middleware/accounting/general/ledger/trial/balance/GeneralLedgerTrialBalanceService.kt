package com.cynergisuite.middleware.accounting.general.ledger.trial.balance

import com.cynergisuite.domain.GeneralLedgerTrialBalanceReportFilterRequest
import com.cynergisuite.middleware.accounting.general.ledger.trial.balance.infrastructure.GeneralLedgerTrialBalanceRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class GeneralLedgerTrialBalanceService @Inject constructor(
   private val generalLedgerTrialBalanceRepository: GeneralLedgerTrialBalanceRepository,
) {
   fun fetchReport(company: CompanyEntity, filterRequest: GeneralLedgerTrialBalanceReportFilterRequest): GeneralLedgerTrialBalanceReportTemplate {
      return GeneralLedgerTrialBalanceReportTemplate(generalLedgerTrialBalanceRepository.fetchReport(company, filterRequest))
   }
}
