package com.cynergisuite.middleware.audit

import com.cynergisuite.domain.Entity
import com.cynergisuite.middleware.audit.action.AuditAction
import com.cynergisuite.middleware.audit.status.AuditStatus
import com.cynergisuite.middleware.store.StoreEntity
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.collections.MutableSet
import kotlin.collections.asSequence

data class Audit (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val store: StoreEntity,
   val actions: MutableSet<AuditAction> = LinkedHashSet()
) : Entity<Audit> {

   constructor(id: Long, audit: Audit) :
      this(
         id = id,
         store = audit.store,
         actions = audit.actions
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Audit = copy()

   fun currentStatus(): AuditStatus =
      actions.asSequence()
         .sortedBy { it.id }
         .map { it.status }
         .last()

   fun printLocation() : String = "${store.number}  ${store.name}"
}
