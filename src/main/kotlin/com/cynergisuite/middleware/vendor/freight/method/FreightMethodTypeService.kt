package com.cynergisuite.middleware.vendor.freight.method

import com.cynergisuite.middleware.vendor.freight.infrastructure.FreightMethodTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreightMethodTypeService @Inject constructor(
   private val freightMethodTypeRepository: FreightMethodTypeRepository
) {

   fun exists(value: String): Boolean =
      freightMethodTypeRepository.exists(value)

   fun fetchAll(): List<FreightMethodTypeEntity> =
      freightMethodTypeRepository.findAll()
}
