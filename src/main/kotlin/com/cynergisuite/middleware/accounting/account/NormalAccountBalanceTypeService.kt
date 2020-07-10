package com.cynergisuite.middleware.accounting.account

import com.cynergisuite.middleware.accounting.account.infrastructure.NormalAccountBalanceTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NormalAccountBalanceTypeService @Inject constructor(
   private val normalAccountBalanceTypeRepository: NormalAccountBalanceTypeRepository
) {

   fun exists(value: String): Boolean =
      normalAccountBalanceTypeRepository.exists(value)

   fun fetchAll(): List<NormalAccountBalanceType> =
      normalAccountBalanceTypeRepository.findAll()
}
