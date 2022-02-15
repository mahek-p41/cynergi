package com.cynergisuite.middleware.accounting.financial.calendar.type

import com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure.OverallPeriodTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class OverallPeriodTypeService @Inject constructor(
   private val repository: OverallPeriodTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<OverallPeriodType> =
      repository.findAll()
}
