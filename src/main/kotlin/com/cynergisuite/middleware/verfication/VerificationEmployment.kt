package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.LegacyIdentifiable
import java.time.LocalDate
import java.time.OffsetDateTime

data class VerificationEmployment(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val department: String?,
   val hireDate: LocalDate?,
   val leaveMessage: Boolean?,
   val name: String?,
   val reliable: Boolean?,
   val title: String?,
   val verification: LegacyIdentifiable
) : LegacyIdentifiable {

   constructor(dto: VerificationEmploymentValueObject, verification: LegacyIdentifiable) :
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

   override fun myId(): Long? = id

   override fun toString(): String {
      return "VerificationEmployment(id=$id, timeCreated=$timeCreated, timeUpdated=$timeUpdated, department=$department, hireDate=$hireDate, leaveMessage=$leaveMessage, name=$name, reliable=$reliable, title=$title, verification=${verification.myId()})"
   }
}
