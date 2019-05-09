package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.localization.MessageCodes.Validation.POSITIVE
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VerificationReferenceValueObject (

   @field:Positive(message = POSITIVE)
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

) : ValueObjectBase<VerificationReferenceValueObject>() {

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

   override fun copyMe(): VerificationReferenceValueObject = copy()

   override fun dtoId(): Long? = id
}

