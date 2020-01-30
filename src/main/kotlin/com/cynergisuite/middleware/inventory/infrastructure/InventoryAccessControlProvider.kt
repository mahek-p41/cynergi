package com.cynergisuite.middleware.inventory.infrastructure

import com.cynergisuite.middleware.authentication.infrastructure.DatasetLimitingAccessControlProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryAccessControlProvider @Inject constructor(
   private val inventoryRepository: InventoryRepository
): DatasetLimitingAccessControlProvider(inventoryRepository)
