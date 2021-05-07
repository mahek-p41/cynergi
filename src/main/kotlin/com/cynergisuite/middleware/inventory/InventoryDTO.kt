package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.inventory.location.InventoryLocationTypeValueObject
import com.cynergisuite.middleware.json.view.Full
import com.cynergisuite.middleware.json.view.InventoryApp
import com.cynergisuite.middleware.store.StoreDTO
import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@JsonView
@Schema(name = "Inventory", title = "Inventory Item", description = "Single item in inventory")
data class InventoryDTO(

   @field:JsonView(value = [Full::class, InventoryApp::class])
   @field:Schema(name = "id", description = "System generated Unique id")
   val id: Long,

   @field:JsonView(value = [Full::class, InventoryApp::class])
   @field:Schema(name = "serialNumber", description = "Either a manufacturer defined serial number or a system generated serial number")
   val serialNumber: String,

   @field:JsonView(value = [Full::class, InventoryApp::class])
   @field:Schema(name = "lookupKey", description = "A system managed key that can be used to lookup inventory items")
   val lookupKey: String,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "lookupKeyType", description = "Defines where the value in lookupKey came from", example = "SERIAL", allowableValues = ["SERIAL", "ALT_ID"])
   val lookupKeyType: String,

   @field:JsonView(value = [Full::class, InventoryApp::class])
   @field:Schema(name = "barcode", description = "System generated value for a single inventory item. One possible value for lookupKey")
   val barcode: String,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "altId", description = "Alternate Identifier.  One possible value for lookupKey")
   val altId: String,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "brand", description = "Manufacturer of the referenced inventory item", nullable = true)
   val brand: String?,

   @field:JsonView(value = [Full::class, InventoryApp::class])
   @field:Schema(name = "modelNumber", description = "Manufacturer defined model number")
   val modelNumber: String,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "productCode", description = "Product code")
   val productCode: String,

   @field:JsonView(value = [Full::class, InventoryApp::class])
   @field:Schema(name = "description", description = "Describes the referenced inventory item")
   val description: String,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "receivedDate", description = "Date item was received into inventory")
   val receivedDate: LocalDate?,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "originalCost", description = "Cost of inventory item when it was entered into inventory")
   val originalCost: BigDecimal,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "actualCost", description = "Cost of item currently")
   val actualCost: BigDecimal,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "modelCategory", description = "Model category")
   val modelCategory: String,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "timesRented", description = "How many times the referenced inventory item has been rented")
   val timesRented: Int,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "totalRevenue", description = "How much revenue has the reference inventory item generated")
   val totalRevenue: BigDecimal,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "remainingValue", description = "Remaining Value")
   val remainingValue: BigDecimal,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "sellPrice", description = "Sell Price")
   val sellPrice: BigDecimal,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "assignedValue", description = "Assigned Value")
   val assignedValue: BigDecimal,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "idleDays", description = "Days since item was last rented")
   val idleDays: Int,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "condition", description = "Condition of item", nullable = true)
   val condition: String?,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "returnedDate", description = "Date that referenced inventory item was returned")
   val returnedDate: LocalDate?,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "location", description = "Current location that the referenced inventory item is stored at", nullable = true)
   val location: StoreDTO?,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "status", description = "Status of referenced inventory item")
   val status: String,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "primaryLocation", description = "Location of where the referenced inventory item is actively managed")
   val primaryLocation: StoreDTO,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "locationType", description = "Location Type")
   val locationType: InventoryLocationTypeValueObject,

   @field:JsonView(value = [Full::class])
   @field:Schema(name = "dataset", description = "dataset item is associated with")
   val dataset: String

) : Identifiable {

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
         location = item.location?.let { StoreDTO(it) },
         status = item.status,
         primaryLocation = StoreDTO(item.primaryLocation),
         locationType = locationType,
         dataset = item.primaryLocation.myCompany().myDataset()
      )

   override fun myId(): Long? = id
}
