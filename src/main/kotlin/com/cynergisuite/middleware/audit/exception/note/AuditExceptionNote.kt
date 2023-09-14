package com.cynergisuite.middleware.audit.exception.note

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableEntity
import com.cynergisuite.middleware.employee.EmployeeEntity
import io.micronaut.core.annotation.Introspected
import java.time.OffsetDateTime
import java.util.UUID

@Introspected
data class AuditExceptionNote(
   val id: UUID? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val note: String,
   val enteredBy: EmployeeEntity,
   val auditException: Identifiable
) : Identifiable {

   constructor(vo: AuditExceptionNoteValueObject, enteredBy: EmployeeEntity, auditId: UUID) :
      this(
         id = vo.id,
         note = vo.note!!,
         enteredBy = enteredBy,
         auditException = SimpleIdentifiableEntity(auditId)
      )

   constructor(vo: AuditExceptionNoteValueObject, enteredBy: EmployeeEntity, auditException: Identifiable) :
      this(
         vo = vo,
         enteredBy = enteredBy,
         auditId = auditException.myId()!!
      )

   override fun myId(): UUID? = id
}
