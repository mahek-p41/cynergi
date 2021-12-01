package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.inventory.InventoryEntity
import io.micronaut.core.annotation.Introspected
import java.time.OffsetDateTime
import java.util.UUID

@Introspected
data class AuditDetailEntity(
   val id: UUID? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val scanArea: AuditScanAreaEntity,
   val lookupKey: String,
   val barcode: String?,
   val serialNumber: String?,
   val productCode: String,
   val altId: String?,
   val inventoryBrand: String?,
   val inventoryModel: String,
   val scannedBy: EmployeeEntity,
   val audit: Identifiable
) : Identifiable {

   constructor(id: UUID?, inventory: InventoryEntity, audit: SimpleIdentifiableEntity, scanArea: AuditScanAreaEntity, scannedBy: EmployeeEntity) :
      this(
         id = id,
         scanArea = scanArea,
         lookupKey = inventory.lookupKey,
         barcode = inventory.barcode,
         serialNumber = inventory.serialNumber,
         productCode = inventory.productCode,
         altId = inventory.altId,
         inventoryBrand = inventory.brand,
         inventoryModel = inventory.modelNumber,
         scannedBy = scannedBy,
         audit = audit
      )

   override fun myId(): UUID? = id
}
