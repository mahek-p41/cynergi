package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceSelectedTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountPayableInvoiceSelectedTypeService @Inject constructor(
   private val repository: AccountPayableInvoiceSelectedTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<AccountPayableInvoiceSelectedType> =
      repository.findAll()
}
