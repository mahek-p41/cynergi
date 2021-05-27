package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanAreaEntity
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.inventory.InventoryEntity
import java.time.OffsetDateTime

data class AuditExceptionEntity(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val scanArea: AuditScanAreaEntity?,
   val barcode: String?,
   val productCode: String?,
   val altId: String?,
   val serialNumber: String?,
   val inventoryBrand: String?,
   val inventoryModel: String?,
   val scannedBy: EmployeeEntity, // FIXME convert to Employee
   val exceptionCode: String,
   val approved: Boolean = false,
   val approvedBy: EmployeeEntity? = null,
   val lookupKey: String,
   val notes: MutableList<AuditExceptionNote> = mutableListOf(),
   val audit: Identifiable
) : Identifiable {

   constructor(audit: Long, inventory: InventoryEntity, scanArea: AuditScanAreaEntity?, scannedBy: EmployeeEntity, exceptionCode: String) :
      this(
         scanArea = scanArea,
         barcode = inventory.barcode,
         productCode = inventory.productCode,
         altId = inventory.altId,
         serialNumber = inventory.serialNumber,
         inventoryBrand = inventory.brand,
         inventoryModel = inventory.modelNumber,
         scannedBy = scannedBy,
         exceptionCode = exceptionCode,
         lookupKey = inventory.lookupKey,
         audit = SimpleIdentifiableEntity(audit)
      )

   constructor(audit: Long, lookupKey: String, scanArea: AuditScanAreaEntity?, scannedBy: EmployeeEntity, exceptionCode: String) :
      this(
         scanArea = scanArea,
         barcode = null,
         productCode = null,
         altId = null,
         serialNumber = null,
         inventoryBrand = null,
         inventoryModel = null,
         scannedBy = scannedBy,
         exceptionCode = exceptionCode,
         lookupKey = lookupKey,
         audit = SimpleIdentifiableEntity(audit)
      )

   override fun myId(): Long? = id
}
