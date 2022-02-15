package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.company.CompanyEntity
import com.cynergisuite.middleware.inventory.infrastructure.InventoryPageRequest
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.inventory.location.InventoryLocationTypeValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import jakarta.inject.Singleton
import java.util.Locale

@Singleton
class InventoryService(
   private val inventoryRepository: InventoryRepository,
   private val localizationService: LocalizationService
) {

   fun fetchAll(pageRequest: InventoryPageRequest, company: CompanyEntity, locale: Locale): Page<InventoryDTO> {
      val inventory = inventoryRepository.findAll(pageRequest, company)

      return inventory.toPage { item ->
         InventoryDTO(
            item,
            InventoryLocationTypeValueObject(item.locationType, item.locationType.localizeMyDescription(locale, localizationService))
         )
      }
   }

   fun fetchByLookupKey(lookupKey: String, company: CompanyEntity, locale: Locale): InventoryDTO? {
      return inventoryRepository.findByLookupKey(lookupKey, company)?.let { map(it, locale) }
   }

   private fun map(inventory: InventoryEntity, locale: Locale): InventoryDTO =
      InventoryDTO(
         inventory,
         InventoryLocationTypeValueObject(inventory.locationType, inventory.locationType.localizeMyDescription(locale, localizationService))
      )
}
