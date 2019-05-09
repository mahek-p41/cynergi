package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.IdentifiableEntity
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

data class VerificationEmployment(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val department: String?,
   val hireDate: LocalDate?,
   val leaveMessage: Boolean?,
   val name: String?,
   val reliable: Boolean?,
   val title: String?,
   val verification: IdentifiableEntity
) : Entity<VerificationEmployment> {
   constructor(dto: VerificationEmploymentValueObject, verification: IdentifiableEntity) :
      this(
         id = dto.id,
         department = dto.department,
         hireDate = dto.hireDate,
         leaveMessage = dto.leaveMessage,
         name = dto.name,
         reliable = dto.reliable,
         title = dto.title,
         verification = verification
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): VerificationEmployment = copy()

   override fun toString(): String {
      return "VerificationEmployment(id=$id, uuRowId=$uuRowId, timeCreated=$timeCreated, timeUpdated=$timeUpdated, department=$department, hireDate=$hireDate, leaveMessage=$leaveMessage, name=$name, reliable=$reliable, title=$title, verification=${verification.entityId()})"
   }
}
