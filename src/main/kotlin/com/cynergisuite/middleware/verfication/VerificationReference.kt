package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.Identifiable
import java.time.OffsetDateTime
import java.util.Objects
import java.util.UUID

data class VerificationReference (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val address: Boolean?,
   val hasHomePhone: Boolean?,
   val known: Int?,  // years known?
   val leaveMessage: Boolean?,
   val rating: String?,
   val relationship: Boolean?,
   val reliable: Boolean?,
   val timeFrame: Int?, // what is this?
   val verifyPhone: Boolean?,
   val verification: Identifiable
) : Entity<VerificationReference> {
   constructor(dto: VerificationReferenceValueObject, parent: Identifiable) :
      this(
         id = dto.id,
         address = dto.address,
         hasHomePhone = dto.hasHomePhone,
         known = dto.known,
         leaveMessage = dto.leaveMessage,
         rating = dto.rating,
         relationship = dto.relationship,
         reliable = dto.reliable,
         timeFrame = dto.timeFrame,
         verifyPhone = dto.verifyPhone,
         verification = parent
      )

   override fun myId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): VerificationReference = copy()

   override fun hashCode(): Int = Objects.hashCode(uuRowId)

   override fun equals(other: Any?): Boolean {
      return when {
          this === other -> true
          other is VerificationReference -> this.uuRowId == other.uuRowId
          else -> false
      }
   }

   override fun toString(): String {
      return "VerificationReference(id=$id, uuRowId=$uuRowId, timeCreated=$timeCreated, timeUpdated=$timeUpdated, address=$address, hasHomePhone=$hasHomePhone, known=$known, leaveMessage=$leaveMessage, rating=$rating, relationship=$relationship, reliable=$reliable, timeFrame=$timeFrame, verifyPhone=$verifyPhone, verification=${verification.myId()})"
   }
}
