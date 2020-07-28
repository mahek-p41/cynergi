package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PrintCurrencyIndicatorTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrintCurrencyIndicatorTypeService @Inject constructor(
   private val repository: PrintCurrencyIndicatorTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<PrintCurrencyIndicatorType> =
      repository.findAll()
}
