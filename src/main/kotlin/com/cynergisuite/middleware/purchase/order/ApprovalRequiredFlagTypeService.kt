package com.cynergisuite.middleware.purchase.order

import com.cynergisuite.middleware.purchase.order.infrastructure.ApprovalRequiredFlagTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApprovalRequiredFlagTypeService @Inject constructor(
   private val repository: ApprovalRequiredFlagTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<ApprovalRequiredFlagType> =
      repository.findAll()
}
