package com.cynergisuite.middleware.accounting.financial.calendar.type

import com.cynergisuite.middleware.accounting.financial.calendar.type.infrastructure.OverallPeriodTypeRepository
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
