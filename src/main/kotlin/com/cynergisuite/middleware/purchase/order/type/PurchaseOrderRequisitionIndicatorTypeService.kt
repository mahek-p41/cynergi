package com.cynergisuite.middleware.purchase.order.type

import com.cynergisuite.middleware.purchase.order.type.infrastructure.PurchaseOrderRequisitionIndicatorTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class PurchaseOrderRequisitionIndicatorTypeService @Inject constructor(
   private val repository: PurchaseOrderRequisitionIndicatorTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<PurchaseOrderRequisitionIndicatorType> =
      repository.findAll()
}
