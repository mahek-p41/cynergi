package com.cynergisuite.middleware.vendor

import com.cynergisuite.middleware.vendor.infrastructure.VendorTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class VendorTypeService @Inject constructor(
   private val vendorTypeRepository: VendorTypeRepository
) {

   fun exists(value: String): Boolean =
      vendorTypeRepository.exists(value)

   fun fetchAll(): List<VendorType> =
      vendorTypeRepository.findAll()
}
