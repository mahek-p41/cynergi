package com.cynergisuite.middleware.purchase.order.type

import com.cynergisuite.middleware.purchase.order.type.infrastructure.PurchaseOrderStatusTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class PurchaseOrderStatusTypeService @Inject constructor(
   private val repository: PurchaseOrderStatusTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<PurchaseOrderStatusType> =
      repository.findAll()
}
