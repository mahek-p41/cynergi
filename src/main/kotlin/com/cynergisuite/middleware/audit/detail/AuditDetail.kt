package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.IdentifiableEntity
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.inventory.Inventory
import java.time.OffsetDateTime
import java.util.UUID

data class AuditDetail(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val scanArea: AuditScanArea,
   val barcode: String,
   val serialNumber: String,
   val productCode: String,
   val altId: String,
   val inventoryBrand: String?,
   val inventoryModel: String,
   val scannedBy: Employee,
   val audit: IdentifiableEntity
) : Entity<AuditDetail> {

   constructor(inventory: Inventory, audit: SimpleIdentifiableEntity, scanArea: AuditScanArea, scannedBy: Employee) :
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

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditDetail = copy()
}
