package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import javax.validation.constraints.Digits
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class VerificationLandlordValueObject (

   @field:Positive
   var id: Long? = null,

   @field:JsonProperty("land_address")
   var address: Boolean? = null,

   @field:Size(max = 18)
   @field:JsonProperty("land_alt_phone")
   var altPhone: String?,

   @field:Size(max = 25)
   @field:JsonProperty("land_lease_type")
   var leaseType: String?,

   @field:JsonProperty("land_leave_msg")
   var leaveMessage: Boolean?,

   @field:Positive
   @field:JsonProperty("land_length")
   var length: String?,

   @field:Size(max = 50)
   @field:JsonProperty("land_name")
   var name: String?,

   @field:Size(max = 15)
   @field:JsonProperty("land_paid_rent")
   var paidRent: String?,

   @field:JsonProperty("land_phone")
   var phone: Boolean?,

   @field:JsonProperty("land_reliable")
   var reliable: Boolean?,

   @field:Digits(integer = 19, fraction = 2)
   @field:JsonProperty("land_rent")
   var rent: BigDecimal?

) : ValueObjectBase<VerificationLandlordValueObject>() {

   constructor(entity: VerificationLandlord) :
      this(
         id = entity.id,
         address = entity.address,
         altPhone = entity.altPhone,
         leaseType = entity.leaseType,
         leaveMessage = entity.leaveMessage,
         length = entity.length,
         name = entity.name,
         paidRent = entity.paidRent,
         phone = entity.phone,
         reliable = entity.reliable,
         rent = entity.rent
      )

   override fun copyMe(): VerificationLandlordValueObject = copy()

   override fun valueObjectId(): Long? = id
}
