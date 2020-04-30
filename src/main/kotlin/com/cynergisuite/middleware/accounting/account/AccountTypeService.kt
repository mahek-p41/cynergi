package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.middleware.accounting.account.infrastructure.AccountTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountTypeService @Inject constructor(
   private val accountTypeRepository: AccountTypeRepository
) {

   fun exists(value: String): Boolean =
      accountTypeRepository.exists(value)

   fun fetchAll(): List<AccountType> =
      accountTypeRepository.findAll()
}
