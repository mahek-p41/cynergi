package com.cynergisuite.middleware.inventory


import com.cynergisuite.middleware.inventory.infrastructure.InventoryRepository
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class InventoryDataLoaderService {
   @Inject InventoryRepository inventoryRepository

   void loadInventory() {
      inventoryRepository.loadInventoryTable()
   }
}
