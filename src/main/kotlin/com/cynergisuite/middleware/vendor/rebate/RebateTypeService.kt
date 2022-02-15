package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.middleware.vendor.rebate.infrastructure.RebateTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class RebateTypeService @Inject constructor(
   private val repository: RebateTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<RebateType> =
      repository.findAll()
}
