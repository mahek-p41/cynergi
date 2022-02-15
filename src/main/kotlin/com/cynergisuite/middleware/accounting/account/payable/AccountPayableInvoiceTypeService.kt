package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountPayableInvoiceTypeService @Inject constructor(
   private val repository: AccountPayableInvoiceTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<AccountPayableInvoiceType> =
      repository.findAll()
}
