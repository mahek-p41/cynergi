package com.cynergisuite.middleware.vendor.freight.onboard

import com.cynergisuite.middleware.vendor.freight.infrastructure.FreightOnboardTypeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreightOnboardTypeService @Inject constructor(
   private val freightOnboardTypeRepository: FreightOnboardTypeRepository
) {

   fun exists(value: String): Boolean =
      freightOnboardTypeRepository.exists(value)

   fun fetchAll(): List<FreightOnboardTypeEntity> =
      freightOnboardTypeRepository.findAll()
}
