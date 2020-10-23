package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceSelectedTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayableInvoiceSelectedTypeService @Inject constructor(
   private val repository: AccountPayableInvoiceSelectedTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<AccountPayableInvoiceSelectedType> =
      repository.findAll()
}
