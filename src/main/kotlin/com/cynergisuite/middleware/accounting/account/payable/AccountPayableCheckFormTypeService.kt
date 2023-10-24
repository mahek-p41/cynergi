package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.AccountPayableCheckFormTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountPayableCheckFormTypeService @Inject constructor(
   private val repository: AccountPayableCheckFormTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<AccountPayableCheckFormType> =
      repository.findAll()
}
