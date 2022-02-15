package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.LegacyIdentifiable
import com.cynergisuite.middleware.inventory.location.InventoryLocationType
import com.cynergisuite.middleware.location.Location
import com.cynergisuite.middleware.store.Store
import io.micronaut.core.annotation.Introspected
import java.math.BigDecimal
import java.time.LocalDate

@Introspected
data class InventoryEntity(
   val id: Long,
   val serialNumber: String?,
   val lookupKey: String,
   val lookupKeyType: String,
   val barcode: String?,
   val altId: String?,
   val brand: String?,
   val modelNumber: String,
   val productCode: String,
   val description: String,
   val receivedDate: LocalDate?,
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
) : LegacyIdentifiable {
   constructor(dto: InventoryDTO, location: Location, primaryLocation: Store, locationType: InventoryLocationType) :
      this(
         id = dto.id,
         serialNumber = dto.serialNumber,
         lookupKey = dto.lookupKey,
         lookupKeyType = dto.lookupKeyType,
         barcode = dto.barcode,
         altId = dto.altId,
         brand = dto.brand,
         modelNumber = dto.modelNumber,
         productCode = dto.productCode,
         description = dto.description,
         receivedDate = dto.receivedDate,
         originalCost = dto.originalCost,
         actualCost = dto.actualCost,
         modelCategory = dto.modelCategory,
         timesRented = dto.timesRented,
         totalRevenue = dto.totalRevenue,
         remainingValue = dto.remainingValue,
         sellPrice = dto.sellPrice,
         assignedValue = dto.assignedValue,
         idleDays = dto.idleDays,
         condition = dto.condition,
         returnedDate = dto.returnedDate,
         location = location,
         status = dto.status,
         primaryLocation = primaryLocation,
         locationType = locationType
      )

   override fun myId(): Long = id
}
