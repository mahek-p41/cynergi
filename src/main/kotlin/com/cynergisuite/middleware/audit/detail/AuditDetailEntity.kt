package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.inventory.InventoryEntity
import java.time.OffsetDateTime

data class AuditDetailEntity(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val scanArea: AuditScanArea,
   val barcode: String,
   val serialNumber: String,
   val productCode: String,
   val altId: String,
   val inventoryBrand: String?,
   val inventoryModel: String,
   val scannedBy: EmployeeEntity,
   val audit: Identifiable
) : Identifiable {

   constructor(inventory: InventoryEntity, audit: SimpleIdentifiableEntity, scanArea: AuditScanArea, scannedBy: EmployeeEntity) :
      this(
         scanArea = scanArea,
         barcode = inventory.barcode,
         serialNumber = inventory.serialNumber,
         productCode = inventory.productCode,
         altId = inventory.altId,
         inventoryBrand = inventory.brand,
         inventoryModel = inventory.modelNumber,
         scannedBy = scannedBy,
         audit = audit
      )

   override fun myId(): Long? = id
}
