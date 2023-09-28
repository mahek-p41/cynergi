package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class GeneralLedgerRecurringTypeService @Inject constructor(
   private val repository: GeneralLedgerRecurringTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<GeneralLedgerRecurringType> =
      repository.findAll()
}
