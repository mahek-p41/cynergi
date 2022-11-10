package com.cynergisuite.middleware.accounting.account.payable.cashout

import com.cynergisuite.domain.CashRequirementFilterRequest
import com.cynergisuite.middleware.accounting.account.payable.cashout.infrastructure.AccountPayableCashRequirementsRepository
import com.cynergisuite.middleware.company.CompanyEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountPayableCashRequirementService @Inject constructor(
   private val accountPayableCashRequirementsRepository: AccountPayableCashRequirementsRepository
) {

   fun fetchReport(company: CompanyEntity, filterRequest: CashRequirementFilterRequest): AccountPayableCashRequirementDTO =
      accountPayableCashRequirementsRepository.findAll(company, filterRequest)
}
