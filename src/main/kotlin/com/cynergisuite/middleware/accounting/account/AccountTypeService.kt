package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.middleware.accounting.account.infrastructure.AccountTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountTypeService @Inject constructor(
   private val accountTypeRepository: AccountTypeRepository
) {

   fun exists(value: String): Boolean =
      accountTypeRepository.exists(value)

   fun fetchAll(): List<AccountType> =
      accountTypeRepository.findAll()
}
