package com.cynergisuite.middleware.inventory

import com.cynergisuite.domain.IdentifiableEntity
import java.time.OffsetDateTime

data class Inventory(
   val id: Long,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val serialNumber: String,
   val barcodeNumber: String,
   val location: Int,
   val status: String,
   val makeModelNumber: String,
   val modelCategory: String,
   val productCode: String,
   val description: String
) : IdentifiableEntity {
   override fun entityId(): Long? = id
}
