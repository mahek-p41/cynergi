package com.cynergisuite.middleware.audit.exception.note

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.authentication.User
import com.cynergisuite.middleware.employee.EmployeeEntity
import com.cynergisuite.middleware.employee.EmployeeValueObject
import java.time.OffsetDateTime
import java.util.UUID

data class AuditExceptionNote(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val note: String,
   val enteredBy: EmployeeEntity,
   val auditException: Identifiable
) : Entity<AuditExceptionNote> {

   constructor(vo: AuditExceptionNoteValueObject, enteredBy: User, auditId: Long) :
      this(
         id = vo.id,
         note = vo.note!!,
         enteredBy = EmployeeEntity.from(enteredBy),
         auditException = SimpleIdentifiableEntity(auditId)
      )

   constructor(vo: AuditExceptionNoteValueObject, enteredBy: User, auditException: Identifiable) :
      this(
         vo = vo,
         enteredBy = enteredBy,
         auditId = auditException.myId()!!
      )

   constructor(vo: AuditExceptionNoteValueObject, enteredBy: EmployeeValueObject, auditId: Long) :
      this(
         id = vo.id,
         note = vo.note!!,
         enteredBy = EmployeeEntity(enteredBy),
         auditException = SimpleIdentifiableEntity(auditId)
      )

   override fun myId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditExceptionNote = copy()
}
