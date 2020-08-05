package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.Page
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.inventory.infrastructure.InventoryPageRequest
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.inventory.location.InventoryLocationTypeValueObject
import com.cynergisuite.middleware.localization.LocalizationService
import io.micronaut.validation.Validated
import java.util.Locale
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class InventoryService(
   private val inventoryRepository: InventoryRepository,
   private val localizationService: LocalizationService
) {

   @Validated
   fun fetchAll(@Valid pageRequest: InventoryPageRequest, company: Company, locale: Locale): Page<InventoryDTO> {
      val inventory = inventoryRepository.findAll(pageRequest, company)

      return inventory.toPage { item ->
         InventoryDTO(
            item,
            InventoryLocationTypeValueObject(item.locationType, item.locationType.localizeMyDescription(locale, localizationService))
         )
      }
   }

   fun fetchByLookupKey(lookupKey: String, company: Company, locale: Locale): InventoryDTO? {
      return inventoryRepository.findByLookupKey(lookupKey, company)?.let { map(it, locale) }
   }

   private fun map(inventory: InventoryEntity, locale: Locale): InventoryDTO =
      InventoryDTO(
         inventory,
         InventoryLocationTypeValueObject(inventory.locationType, inventory.locationType.localizeMyDescription(locale, localizationService))
      )
}
