package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.middleware.purchase.order.infrastructure.DefaultPurchaseOrderTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPurchaseOrderTypeService @Inject constructor(
   private val repository: DefaultPurchaseOrderTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<DefaultPurchaseOrderType> =
      repository.findAll()
}
