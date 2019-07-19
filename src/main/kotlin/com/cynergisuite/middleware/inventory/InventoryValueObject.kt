package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.ValueObjectBase
import javax.validation.constraints.NotNull

data class InventoryValueObject(

   @field:NotNull
   var id: Long,

   @field:NotNull
   var serialNumber: String,

   @field:NotNull
   var barcodeNumber: String,

   @field:NotNull
   var location: Int,

   @field:NotNull
   var status: String,

   @field:NotNull
   var makeModelNumber: String,

   @field:NotNull
   var modelCategory: String,

   @field:NotNull
   var productCode: String,

   @field:NotNull
   var description: String

) : ValueObjectBase<InventoryValueObject>() {

   constructor(item: Inventory) :
      this(
         id = item.id,
         serialNumber = item.serialNumber,
         barcodeNumber = item.barcodeNumber,
         location = item.location,
         status = item.status,
         makeModelNumber = item.makeModelNumber,
         modelCategory = item.modelCategory,
         productCode = item.productCode,
         description = item.description
      )

   override fun valueObjectId(): Long? = id
   override fun copyMe(): InventoryValueObject = copy()
}
