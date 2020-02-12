package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.company.Company
import com.cynergisuite.middleware.store.StoreEntity
import java.time.OffsetDateTime
import java.util.UUID

data class AuditEntity(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val store: StoreEntity,
   val number: Int = 0,
   val totalDetails: Int = 0,
   val totalExceptions: Int = 0,
   val hasExceptionNotes: Boolean = false,
   val lastUpdated: OffsetDateTime? = null,
   val inventoryCount: Int = 0,
   val company: Company,
   val actions: MutableSet<AuditActionEntity> = LinkedHashSet()
) : Entity<AuditEntity> {

   constructor(id: Long, audit: AuditEntity) :
      this(
         id = id,
         store = audit.store,
         number = audit.number,
         actions = audit.actions,
         totalDetails = audit.totalDetails,
         totalExceptions = audit.totalExceptions,
         hasExceptionNotes = audit.hasExceptionNotes,
         inventoryCount = audit.inventoryCount,
         lastUpdated = audit.lastUpdated,
         company = audit.company
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditEntity = copy()

   fun currentStatus(): AuditStatus =
      actions.asSequence()
         .sortedBy { it.id }
         .map { it.status }
         .last()

   fun printLocation() : String = "${store.number}  ${store.name}"
}
