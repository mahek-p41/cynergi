package com.cynergisuite.middleware.audit.exception.note

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.IdentifiableEntity
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.employee.Employee
import com.cynergisuite.middleware.employee.EmployeeValueObject
import java.time.OffsetDateTime
import java.util.UUID

data class AuditExceptionNote(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val note: String,
   val enteredBy: Employee,
   val auditException: IdentifiableEntity
) : Entity<AuditExceptionNote> {

   constructor(vo: AuditExceptionNoteValueObject, enteredBy: EmployeeValueObject, auditId: Long) :
      this(
         id = vo.id,
         note = vo.note!!,
         enteredBy = Employee(enteredBy),
         auditException = SimpleIdentifiableEntity(auditId)
      )
   
   constructor(vo: AuditExceptionNoteValueObject, enteredBy: EmployeeValueObject, auditException: IdentifiableEntity) :
      this(
         vo = vo,
         enteredBy = enteredBy,
         auditId = auditException.entityId()!!
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): AuditExceptionNote = copy()
}
