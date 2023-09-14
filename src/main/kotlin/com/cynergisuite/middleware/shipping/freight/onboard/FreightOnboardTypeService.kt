package com.cynergisuite.middleware.shipping.freight.onboard

import com.cynergisuite.middleware.shipping.freight.onboard.infrastructure.FreightOnboardTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class FreightOnboardTypeService @Inject constructor(
   private val freightOnboardTypeRepository: FreightOnboardTypeRepository
) {

   fun exists(value: String): Boolean =
      freightOnboardTypeRepository.exists(value)

   fun fetchAll(): List<FreightOnboardType> =
      freightOnboardTypeRepository.findAll()
}
