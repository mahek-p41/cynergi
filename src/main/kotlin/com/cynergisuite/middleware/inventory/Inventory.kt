package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.IdentifiableEntity
import com.cynergisuite.middleware.inventory.location.InventoryLocationType
import com.cynergisuite.middleware.store.Store
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime

data class Inventory(
   val id: Long,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
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
   val location: Store?,
   val status: String,
   val primaryLocation: Store,
   val locationType: InventoryLocationType
) : IdentifiableEntity {
   override fun entityId(): Long? = id
}
