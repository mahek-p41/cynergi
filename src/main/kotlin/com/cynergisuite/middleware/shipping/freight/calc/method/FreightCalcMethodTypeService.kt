package com.cynergisuite.middleware.shipping.freight.calc.method

import com.cynergisuite.middleware.shipping.freight.calc.method.infrastructure.FreightCalcMethodTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreightCalcMethodTypeService @Inject constructor(
   private val freightCalcMethodTypeRepository: FreightCalcMethodTypeRepository
) {
   fun fetchAll(): List<FreightCalcMethodType> =
      freightCalcMethodTypeRepository.findAll()
}
