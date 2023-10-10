package com.cynergisuite.middleware.accounting.account.payable.recurring

import com.cynergisuite.middleware.accounting.account.payable.recurring.infrastructure.ExpenseMonthCreationTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ExpenseMonthCreationTypeService @Inject constructor(
   private val repository: ExpenseMonthCreationTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<ExpenseMonthCreationType> =
      repository.findAll()
}