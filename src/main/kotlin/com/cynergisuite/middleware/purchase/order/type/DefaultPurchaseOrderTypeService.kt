package com.cynergisuite.middleware.purchase.order.type

import com.cynergisuite.middleware.purchase.order.type.infrastructure.DefaultPurchaseOrderTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class DefaultPurchaseOrderTypeService @Inject constructor(
   private val repository: DefaultPurchaseOrderTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<DefaultPurchaseOrderType> =
      repository.findAll()
}
