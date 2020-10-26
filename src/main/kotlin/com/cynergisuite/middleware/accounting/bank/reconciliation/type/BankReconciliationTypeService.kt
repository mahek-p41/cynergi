package com.cynergisuite.middleware.accounting.bank.reconciliation.type

import com.cynergisuite.middleware.accounting.bank.reconciliation.type.infrastructure.BankReconciliationTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankReconciliationTypeService @Inject constructor(
   private val repository: BankReconciliationTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<BankReconciliationType> =
      repository.findAll()
}
