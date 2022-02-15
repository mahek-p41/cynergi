package com.cynergisuite.middleware.shipping.location

import com.cynergisuite.middleware.shipping.location.infrastructure.ShipLocationTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ShipLocationTypeService @Inject constructor(
   private val shipLocationTypeRepository: ShipLocationTypeRepository
) {

   fun exists(value: String): Boolean =
      shipLocationTypeRepository.exists(value)

   fun fetchAll(): List<ShipLocationType> =
      shipLocationTypeRepository.findAll()
}
