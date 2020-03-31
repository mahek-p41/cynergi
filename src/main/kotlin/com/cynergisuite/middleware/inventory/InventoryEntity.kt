package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.inventory.location.InventoryLocationType
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.store.Store
import com.cynergisuite.middleware.store.StoreEntity
import java.math.BigDecimal
import java.time.LocalDate

data class InventoryEntity(
   val id: Long,
   val serialNumber: String,
   val lookupKey: String,
   val lookupKeyType: String,
   val barcode: String,
   val altId: String,
   val brand: String?,
   val modelNumber: String,
   val productCode: String,
   val description: String,
   val receivedDate: LocalDate,
   val originalCost: BigDecimal,
   val actualCost: BigDecimal,
   val modelCategory: String,
   val timesRented: Int,
   val totalRevenue: BigDecimal,
   val remainingValue: BigDecimal,
   val sellPrice: BigDecimal,
   val assignedValue: BigDecimal,
   val idleDays: Int,
   val condition: String?,
   val returnedDate: LocalDate?,
   val location: Location?,
   val status: String,
   val primaryLocation: Store,
   val locationType: InventoryLocationType
) : Identifiable {
   override fun myId(): Long? = id
}
