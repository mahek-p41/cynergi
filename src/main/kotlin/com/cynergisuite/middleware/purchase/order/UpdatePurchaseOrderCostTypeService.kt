package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.middleware.purchase.order.infrastructure.UpdatePurchaseOrderCostTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdatePurchaseOrderCostTypeService @Inject constructor(
   private val repository: UpdatePurchaseOrderCostTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<UpdatePurchaseOrderCostType> =
      repository.findAll()
}
