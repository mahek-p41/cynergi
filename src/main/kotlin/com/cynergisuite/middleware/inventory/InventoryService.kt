package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.Page
import com.cynergisuite.domain.PageRequest
import com.cynergisuite.middleware.inventory.infrastructure.InventoryPageRequest
import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreValueObject
import javax.inject.Singleton

@Singleton
class InventoryService(
   private val inventoryRepository: InventoryRepository
) {
   fun fetchAll(pageRequest: InventoryPageRequest): Page<InventoryValueObject> {
      val inventory = inventoryRepository.findAll(pageRequest)

      return inventory.toPage(pageRequest) { item ->
         InventoryValueObject(item)
      }
   }
}
