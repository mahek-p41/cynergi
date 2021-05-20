package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.LegacyIdentifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "VerificationReference", title = "Reference Verification for a customer", description = "Reference verification for a single customer associated with a Verification")
data class VerificationReferenceValueObject(

   @field:Positive
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

) : LegacyIdentifiable {

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

   override fun myId(): Long? = id
}
