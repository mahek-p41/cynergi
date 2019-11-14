package com.cynergisuite.middleware.audit.exception

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.audit.exception.note.AuditExceptionNote
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.EmployeeValueObject
import com.cynergisuite.middleware.inventory.InventoryEntity
import java.time.OffsetDateTime
import java.util.UUID

data class AuditExceptionEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val scanArea: AuditScanArea?,
   val barcode: String,
   val productCode: String?,
   val altId: String?,
   val serialNumber: String?,
   val inventoryBrand: String?,
   val inventoryModel: String?,
   val scannedBy: Employee,
   val exceptionCode: String,
   val signedOff: Boolean = false,
   val lookupKey: String?,
   val notes: MutableList<AuditExceptionNote> = mutableListOf(),
   val audit: Identifiable
) : Entity<AuditExceptionEntity> {

   constructor(vo: AuditExceptionValueObject, scanArea: AuditScanArea?) :
      this (
         id = vo.id,
         scanArea = scanArea,
         barcode = vo.barcode!!,
         productCode = vo.productCode,
         altId = vo.altId,
         serialNumber = vo.serialNumber,
         inventoryBrand = vo.inventoryBrand,
         inventoryModel = vo.inventoryModel,
         scannedBy = Employee(vo.scannedBy!!),
         exceptionCode = vo.exceptionCode!!,
         signedOff = vo.signedOff,
         lookupKey = vo.lookupKey,
         notes = vo.notes.asSequence().map { AuditExceptionNote(it,it.enteredBy!!, vo.audit!!.myId()!!) }.toMutableList(),
         audit = SimpleIdentifiableEntity(vo.audit!!)
      )

   constructor(audit: Long, inventory: InventoryEntity, scanArea: AuditScanArea?, scannedBy: EmployeeValueObject, exceptionCode: String) :
      this(
         scanArea = scanArea,
         barcode = inventory.barcode,
         productCode = inventory.productCode,
         altId = inventory.altId,
         serialNumber = inventory.serialNumber,
         inventoryBrand = inventory.brand,
         inventoryModel = inventory.modelNumber,
         scannedBy = Employee(scannedBy),
         exceptionCode = exceptionCode,
         lookupKey = inventory.lookupKey,
         audit = SimpleIdentifiableEntity(audit)
      )

   constructor(audit: Long, barcode: String, scanArea: AuditScanArea?, scannedBy: EmployeeValueObject, exceptionCode: String) :
      this(
         scanArea = scanArea,
         barcode = barcode,
         productCode = null,
         altId = null,
         serialNumber = null,
         inventoryBrand = null,
         inventoryModel = null,
         scannedBy = Employee(scannedBy),
         exceptionCode = exceptionCode,
         lookupKey = null,
         audit = SimpleIdentifiableEntity(audit)
      )

   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditExceptionEntity = copy()
   override fun myId(): Long? = id
}
