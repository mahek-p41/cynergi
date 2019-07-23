package com.cynergisuite.middleware.audit.discrepancy

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.IdentifiableEntity
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.employee.Employee
import java.time.OffsetDateTime
import java.util.UUID

data class AuditDiscrepancy(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val barCode: String,
   val inventoryId: String,
   val inventoryBrand: String,
   val inventoryModel: String,
   val scannedBy: Employee,
   val notes: String,
   val audit: IdentifiableEntity
) : Entity<AuditDiscrepancy> {

   constructor(vo: AuditDiscrepancyValueObject) :
      this (
         id = vo.id,
         barCode = vo.barCode!!,
         inventoryId = vo.inventoryId!!,
         inventoryBrand = vo.inventoryBrand!!,
         inventoryModel = vo.inventoryModel!!,
         scannedBy = Employee(vo.scannedBy!!),
         notes = vo.notes!!,
         audit = SimpleIdentifiableEntity(vo.audit!!)
      )

   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditDiscrepancy = copy()
   override fun entityId(): Long? = id
}
