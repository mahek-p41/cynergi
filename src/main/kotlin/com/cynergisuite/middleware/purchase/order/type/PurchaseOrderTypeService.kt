package com.cynergisuite.middleware.purchase.order.type

import com.cynergisuite.middleware.purchase.order.type.infrastructure.PurchaseOrderTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class PurchaseOrderTypeService @Inject constructor(
   private val repository: PurchaseOrderTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<PurchaseOrderType> =
      repository.findAll()
}