package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import java.time.OffsetDateTime
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
   val verficationId: Long
) : Entity {

   constructor(dto: VerificationReferenceDto) :
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
         verficationId = dto.verificationId
      )

   override fun entityId(): Long? = id
}

data class VerificationReferenceDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   val address: Boolean?,

   val hasHomePhone: Boolean?,

   val known: Int?,

   val leaveMessage: Boolean?,

   @field:Size(max = 3)
   val rating: String?,

   val relationship: Boolean?,

   val reliable: Boolean?,

   val timeFrame: Int?,

   val verifyPhone: Boolean?,

   val verificationId: Long

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
         verifyPhone = entity.verifyPhone,
         verificationId = entity.verficationId
      )

   override fun copyMe(): VerificationReferenceDto = copy()
}
