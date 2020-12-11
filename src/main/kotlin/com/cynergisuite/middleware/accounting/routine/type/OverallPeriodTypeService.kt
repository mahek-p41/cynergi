package com.cynergisuite.middleware.accounting.routine.type

import com.cynergisuite.middleware.accounting.routine.type.infrastructure.OverallPeriodTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverallPeriodTypeService @Inject constructor(
   private val repository: OverallPeriodTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<OverallPeriodType> =
      repository.findAll()
}
