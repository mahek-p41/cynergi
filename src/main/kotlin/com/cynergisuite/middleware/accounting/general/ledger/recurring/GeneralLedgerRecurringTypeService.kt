package com.cynergisuite.middleware.accounting.general.ledger.recurring

import com.cynergisuite.middleware.accounting.general.ledger.recurring.infrastructure.GeneralLedgerRecurringTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneralLedgerRecurringTypeService @Inject constructor(
   private val repository: GeneralLedgerRecurringTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<GeneralLedgerRecurringType> =
      repository.findAll()
}
