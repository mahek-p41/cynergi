package com.cynergisuite.middleware.purchase.order.type

import com.cynergisuite.middleware.purchase.order.type.infrastructure.ApprovalRequiredFlagTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ApprovalRequiredFlagTypeService @Inject constructor(
   private val repository: ApprovalRequiredFlagTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<ApprovalRequiredFlagType> =
      repository.findAll()
}
