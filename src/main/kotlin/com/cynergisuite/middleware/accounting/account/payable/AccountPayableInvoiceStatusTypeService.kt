package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableInvoiceStatusTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountPayableInvoiceStatusTypeService @Inject constructor(
   private val repository: AccountPayableInvoiceStatusTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<AccountPayableInvoiceStatusType> =
      repository.findAll()
}
