package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.inventory.infrastructure.InventoryPageRequest
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.inventory.location.InventoryLocationTypeValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import java.util.*
import javax.inject.Singleton

@Singleton
class InventoryService(
   private val inventoryRepository: InventoryRepository,
   private val localizationService: LocalizationService
) {
   fun fetchAll(pageRequest: InventoryPageRequest, company: Company, locale: Locale): Page<InventoryValueObject> {
      val inventory = inventoryRepository.findAll(pageRequest, company)

      return inventory.toPage  { item ->
         InventoryValueObject(
            item,
            InventoryLocationTypeValueObject(item.locationType, item.locationType.localizeMyDescription(locale, localizationService))
         )
      }
   }

   fun fetchByLookupKey(lookupKey: String, company: Company, locale: Locale): InventoryValueObject? {
      return inventoryRepository.findByLookupKey(lookupKey, company)?.let { map(it, locale) }
   }

   private fun map(inventory: InventoryEntity, locale: Locale) : InventoryValueObject =
      InventoryValueObject(
         inventory,
         InventoryLocationTypeValueObject(inventory.locationType, inventory.locationType.localizeMyDescription(locale, localizationService))
      )
}
