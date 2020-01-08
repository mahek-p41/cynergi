package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.inventory.location.InventoryLocationType
import com.cynergisuite.middleware.store.StoreEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

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
   val location: StoreEntity?,
   val status: String,
   val primaryLocation: StoreEntity,
   val locationType: InventoryLocationType,
   val dataset: String
) : Identifiable {
   override fun myId(): Long? = id
}
