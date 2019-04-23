package com.cynergisuite.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.cynergisuite.middleware.dto.spi.DataTransferObjectBase
import com.cynergisuite.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import java.time.OffsetDateTime
import java.util.Objects
import java.util.UUID
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

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
   val verification: com.cynergisuite.middleware.entity.IdentifiableEntity
) : com.cynergisuite.middleware.entity.Entity<VerificationReference> {
   constructor(dto: VerificationReferenceDto, parent: com.cynergisuite.middleware.entity.IdentifiableEntity) :
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

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): VerificationReference  = copy()

   override fun hashCode(): Int = Objects.hashCode(uuRowId)

   override fun equals(other: Any?): Boolean {
      return when {
          this === other -> true
          other is VerificationReference -> this.uuRowId == other.uuRowId
          else -> false
      }
   }

   override fun toString(): String {
      return "VerificationReference(id=$id, uuRowId=$uuRowId, timeCreated=$timeCreated, timeUpdated=$timeUpdated, address=$address, hasHomePhone=$hasHomePhone, known=$known, leaveMessage=$leaveMessage, rating=$rating, relationship=$relationship, reliable=$reliable, timeFrame=$timeFrame, verifyPhone=$verifyPhone, verification=${verification.entityId()})"
   }
}

@JsonInclude(NON_NULL)
data class VerificationReferenceDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:JsonProperty("ref_address")
   val address: Boolean?,

   @field:JsonProperty("ref_has_home_phone")
   val hasHomePhone: Boolean?,

   @field:JsonProperty("ref_known")
   val known: Int?,

   @field:JsonProperty("ref_leave_msg")
   val leaveMessage: Boolean?,

   @field:Size(max = 3)
   @field:JsonProperty("ref_rating")
   val rating: String?,

   @field:JsonProperty("ref_relationship")
   val relationship: Boolean?,

   @field:JsonProperty("ref_reliable")
   val reliable: Boolean?,

   @field:JsonProperty("ref_time_frame")
   val timeFrame: Int?,

   @field:JsonProperty("ref_verify_phone")
   val verifyPhone: Boolean?

) : DataTransferObjectBase<VerificationReferenceDto>() {

   constructor(entity: VerificationReference) :
      this(
         id = entity.id,
         address = entity.address,
         hasHomePhone = entity.hasHomePhone,
         known = entity.known,
         leaveMessage = entity.leaveMessage,
         rating = entity.rating,
         relationship = entity.relationship,
         reliable = entity.reliable,
         timeFrame = entity.timeFrame,
         verifyPhone = entity.verifyPhone
      )

   override fun copyMe(): VerificationReferenceDto = copy()

   override fun dtoId(): Long? = id
}
