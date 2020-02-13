package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.inventory.location.InventoryLocationTypeValueObject
import com.cynergisuite.middleware.store.StoreValueObject
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(name = "Inventory", title = "Inventory Item", description = "Single item in inventory")
data class InventoryValueObject(

   @field:Schema(name = "id", description = "System generated Unique id")
   val id: Long,

   @field:Schema(name = "serialNumber", description = "Either a manufacturer defined serial number or a system generated serial number")
   val serialNumber: String,

   @field:Schema(name = "lookupKey", description = "A system managed key that can be used to lookup inventory items")
   val lookupKey: String,

   @field:Schema(name = "lookupKeyType", description = "Defines where the value in lookupKey came from", example = "SERIAL", allowableValues = ["SERIAL", "BARCODE"])
   val lookupKeyType: String,

   @field:Schema(name = "barcode", description = "System generated value for a single inventory item. One possible value for lookupKey")
   val barcode: String,

   @field:Schema(name = "altId", description = "Alternate Identifier.  One possible value for lookupKey")
   val altId: String,

   @field:Schema(name = "brand", description = "Manufacturer of the referenced inventory item", nullable = true)
   val brand: String?,

   @field:Schema(name = "modelNumber", description = "Manufacturer defined model number")
   val modelNumber: String,

   @field:Schema(name = "productCode", description = "Product code")
   val productCode: String,

   @field:Schema(name = "description", description = "Describes the referenced inventory item")
   val description: String,

   @field:Schema(name = "receivedDate", description = "Date item was received into inventory")
   val receivedDate: LocalDate,

   @field:Schema(name = "originalCost", description = "Cost of inventory item when it was entered into inventory")
   val originalCost: BigDecimal,

   @field:Schema(name = "actualCost", description = "Cost of item currently")
   val actualCost: BigDecimal,

   @field:Schema(name = "modelCategory", description = "Model category")
   val modelCategory: String,

   @field:Schema(name = "timesRented", description = "How many times the referenced inventory item has been rented")
   val timesRented: Int,

   @field:Schema(name = "totalRevenue", description = "How much revenue has the reference inventory item generated")
   val totalRevenue: BigDecimal,

   @field:Schema(name = "remainingValue", description = "Remaining Value")
   val remainingValue: BigDecimal,

   @field:Schema(name = "sellPrice", description = "Sell Price")
   val sellPrice: BigDecimal,

   @field:Schema(name = "assignedValue", description = "Assigned Value")
   val assignedValue: BigDecimal,

   @field:Schema(name = "idleDays", description = "Days since item was last rented")
   val idleDays: Int,

   @field:Schema(name = "condition", description = "Condition of item", nullable = true)
   val condition: String?,

   @field:Schema(name = "returnedDate", description = "Date that referenced inventory item was returned")
   val returnedDate: LocalDate?,

   @field:Schema(name = "location", description = "Current location that the referenced inventory item is stored at", nullable = true)
   val location: StoreValueObject?,

   @field:Schema(name = "status", description = "Status of referenced inventory item")
   val status: String,

   @field:Schema(name = "primaryLocation", description = "Location of where the referenced inventory item is actively managed")
   val primaryLocation: StoreValueObject,

   @field:Schema(name = "locationType", description = "Location Type")
   val locationType: InventoryLocationTypeValueObject,

   @field:Schema(name = "dataset", description = "dataset item is associated with")
   val dataset: String

) : ValueObjectBase<InventoryValueObject>() {

   constructor(item: InventoryEntity, locationType: InventoryLocationTypeValueObject) :
      this(
         id = item.id,
         serialNumber = item.serialNumber,
         lookupKey = item.lookupKey,
         lookupKeyType = item.lookupKeyType,
         barcode = item.barcode,
         altId = item.altId,
         brand = item.brand,
         modelNumber = item.modelNumber,
         productCode = item.productCode,
         description = item.description,
         receivedDate = item.receivedDate,
         originalCost = item.originalCost,
         actualCost = item.actualCost,
         modelCategory = item.modelCategory,
         timesRented = item.timesRented,
         totalRevenue = item.totalRevenue,
         remainingValue = item.remainingValue,
         sellPrice = item.sellPrice,
         assignedValue = item.assignedValue,
         idleDays = item.idleDays,
         condition = item.condition,
         returnedDate = item.returnedDate,
         location = item.location?.let { StoreValueObject(it) },
         status = item.status,
         primaryLocation = StoreValueObject(item.primaryLocation),
         locationType = locationType,
         dataset = item.primaryLocation.myCompany().myDataset()
      )

   override fun myId(): Long? = id
   override fun copyMe(): InventoryValueObject = copy()
}
