package com.cynergisuite.middleware.audit.detail

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.IdentifiableEntity
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.audit.Audit
import com.cynergisuite.middleware.audit.detail.scan.area.AuditScanArea
import com.cynergisuite.middleware.employee.Employee
import java.time.OffsetDateTime
import java.util.UUID

data class AuditDetail(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val scanArea: AuditScanArea,
   val barCode: String,
   val inventoryId: String,
   val inventoryBrand: String,
   val inventoryModel: String,
   val scannedBy: Employee,
   val inventoryStatus: String,
   val audit: IdentifiableEntity
) : Entity<AuditDetail> {

   constructor(vo: AuditDetailValueObject, audit: SimpleIdentifiableEntity, scanArea: AuditScanArea, scannedBy: Employee) :
      this(
         id = vo.id,
         scanArea = scanArea,
         barCode = vo.barCode!!,
         inventoryId = vo.inventoryId!!,
         inventoryBrand = vo.inventoryBrand!!,
         inventoryModel = vo.inventoryModel!!,
         scannedBy = scannedBy,
         inventoryStatus = vo.inventoryStatus!!,
         audit = SimpleIdentifiableEntity(audit)
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditDetail = copy()
}
