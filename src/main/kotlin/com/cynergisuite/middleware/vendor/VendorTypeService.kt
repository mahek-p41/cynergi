package com.cynergisuite.middleware.vendor

import com.cynergisuite.middleware.localization.LocalizationService
import com.cynergisuite.middleware.vendor.infrastructure.VendorTypeRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.*

@Singleton
class VendorTypeService @Inject constructor(
   private val vendorTypeRepository: VendorTypeRepository,
   private val localizationService: LocalizationService

) {

   fun exists(value: String): Boolean =
      vendorTypeRepository.exists(value)

   fun fetchAll(locale: Locale): List<VendorTypeDTO> {

      val found = vendorTypeRepository.findAll()

      return found.map {
         VendorTypeDTO(it, it.localizeMyDescription(locale, localizationService))

      }
   }
}
