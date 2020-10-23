package com.cynergisuite.middleware.purchase.order.type

import com.cynergisuite.middleware.purchase.order.type.infrastructure.ExceptionIndicatorTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionIndicatorTypeService @Inject constructor(
   private val repository: ExceptionIndicatorTypeRepository
) {

   fun exists(value: String): Boolean =
      repository.exists(value)

   fun fetchAll(): List<ExceptionIndicatorType> =
      repository.findAll()
}
