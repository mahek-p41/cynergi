package com.cynergisuite.middleware.accounting.bank

import com.cynergisuite.middleware.accounting.bank.infrastructure.BankCurrencyTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankCurrencyTypeService @Inject constructor(
   private val bankCurrencyTypeRepository: BankCurrencyTypeRepository
) {

   fun exists(value: String): Boolean =
      bankCurrencyTypeRepository.exists(value)

   fun fetchAll(): List<BankCurrencyType> =
      bankCurrencyTypeRepository.findAll()
}
