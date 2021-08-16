package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.middleware.audit.action.AuditActionEntity
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.store.Store
import java.time.OffsetDateTime
import java.util.UUID

data class AuditEntity(
   val id: UUID? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val store: Store,
   val number: Int,
   val totalDetails: Int,
   val totalExceptions: Int,
   val hasExceptionNotes: Boolean,
   val lastUpdated: OffsetDateTime? = null,
   val inventoryCount: Int,
   val actions: MutableSet<AuditActionEntity> = LinkedHashSet()
) : Identifiable {

   constructor(id: UUID, audit: AuditEntity) :
      this(
         id = id,
         store = audit.store,
         number = audit.number,
         actions = audit.actions,
         totalDetails = audit.totalDetails,
         totalExceptions = audit.totalExceptions,
         hasExceptionNotes = audit.hasExceptionNotes,
         inventoryCount = audit.inventoryCount,
         lastUpdated = audit.lastUpdated
      )

   override fun myId(): UUID? = id

   fun currentStatus(): AuditStatus =
      actions.asSequence()
         .sortedBy { it.timeCreated }
         .map { it.status }
         .last()

   fun printLocation(): String = "${store.myNumber()}  ${store.myName()}"
}
