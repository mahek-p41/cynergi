package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.inventory.infrastructure.InventoryPageRequest
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.inventory.location.InventoryLocationTypeValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import java.util.Locale
import javax.inject.Singleton

@Singleton
class InventoryService(
   private val inventoryRepository: InventoryRepository,
   private val localizationService: LocalizationService
) {
   fun fetchAll(pageRequest: InventoryPageRequest, dataset: String, locale: Locale): Page<InventoryValueObject> {
      val inventory = inventoryRepository.findAll(pageRequest, dataset)

      return inventory.toPage  { item ->
         InventoryValueObject(
            item,
            InventoryLocationTypeValueObject(item.locationType, item.locationType.localizeMyDescription(locale, localizationService))
         )
      }
   }

   fun fetchByLookupKey(lookupKey: String, dataset: String, locale: Locale): InventoryValueObject? {
      return inventoryRepository.findByLookupKey(lookupKey, dataset)?.let { map(it, locale) }
   }

   private fun map(inventory: InventoryEntity, locale: Locale) : InventoryValueObject =
      InventoryValueObject(
         inventory,
         InventoryLocationTypeValueObject(inventory.locationType, inventory.locationType.localizeMyDescription(locale, localizationService))
      )
}
