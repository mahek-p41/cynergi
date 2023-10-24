package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableRecurringInvoiceStatusTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountPayableRecurringInvoiceStatusTypeService @Inject constructor(
   private val repository: AccountPayableRecurringInvoiceStatusTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<AccountPayableRecurringInvoiceStatusType> =
      repository.findAll()
}
