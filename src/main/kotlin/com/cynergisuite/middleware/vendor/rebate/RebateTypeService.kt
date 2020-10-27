package com.cynergisuite.middleware.vendor.rebate

import com.cynergisuite.middleware.vendor.rebate.infrastructure.RebateTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RebateTypeService @Inject constructor(
   private val repository: RebateTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<RebateType> =
      repository.findAll()
}
