package com.cynergisuite.middleware.accounting.account.payable

import com.cynergisuite.middleware.accounting.account.payable.infrastructure.PurchaseOrderNumberRequiredIndicatorTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseOrderNumberRequiredIndicatorTypeService @Inject constructor(
   private val repository: PurchaseOrderNumberRequiredIndicatorTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<PurchaseOrderNumberRequiredIndicatorType> =
      repository.findAll()
}
