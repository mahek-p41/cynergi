package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountPayableInvoiceStatusTypeService @Inject constructor(
   private val repository: AccountPayableInvoiceStatusTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<AccountPayableInvoiceStatusType> =
      repository.findAll()
}
