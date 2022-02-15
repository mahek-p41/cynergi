package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.middleware.accounting.account.infrastructure.AccountStatusTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AccountStatusTypeService @Inject constructor(
   private val AccountStatusTypeRepository: AccountStatusTypeRepository
) {

   fun exists(value: String): Boolean =
      AccountStatusTypeRepository.exists(value)

   fun fetchAll(): List<AccountStatusType> =
      AccountStatusTypeRepository.findAll()
}
