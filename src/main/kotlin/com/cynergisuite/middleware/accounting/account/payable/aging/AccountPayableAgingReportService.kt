package com.cynergisuite.middleware.accounting.account.payable.aging

import com.cynergisuite.domain.AgingReportFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.aging.infrastructure.AccountPayableAgingReportRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountPayableAgingReportService @Inject constructor(
   private val accountPayableAgingReportRepository: AccountPayableAgingReportRepository
) {

   fun fetchReport(company: CompanyEntity, filterRequest: AgingReportFilterRequest): AccountPayableAgingReportDTO =
      accountPayableAgingReportRepository.findAll(company, filterRequest)
}
