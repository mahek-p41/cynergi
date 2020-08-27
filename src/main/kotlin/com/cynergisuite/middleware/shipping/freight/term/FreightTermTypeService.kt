package com.cynergisuite.middleware.shipping.freight.term

import com.cynergisuite.middleware.shipping.freight.term.infrastructure.FreightTermTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreightTermTypeService @Inject constructor(
   private val freightTermTypeRepository: FreightTermTypeRepository
) {

   fun exists(value: String): Boolean =
      freightTermTypeRepository.exists(value)

   fun fetchAll(): List<FreightTermType> =
      freightTermTypeRepository.findAll()
}
