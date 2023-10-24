package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PrintCurrencyIndicatorTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class PrintCurrencyIndicatorTypeService @Inject constructor(
   private val repository: PrintCurrencyIndicatorTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<PrintCurrencyIndicatorType> =
      repository.findAll()
}
