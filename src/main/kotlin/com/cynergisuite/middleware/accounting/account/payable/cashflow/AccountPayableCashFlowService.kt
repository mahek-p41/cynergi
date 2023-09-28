package com.cynergisuite.middleware.accounting.account.payable.cashflow

import com.cynergisuite.domain.CashFlowFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.cashflow.infrastructure.CashFlowRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountPayableCashFlowService @Inject constructor(
   private val cashFlowRepository: CashFlowRepository
) {

   fun fetchReport(company: CompanyEntity, filterRequest: CashFlowFilterRequest): AccountPayableCashFlowDTO =
      cashFlowRepository.findAll(company, filterRequest)
}
