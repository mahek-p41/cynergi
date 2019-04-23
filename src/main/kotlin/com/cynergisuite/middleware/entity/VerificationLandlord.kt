package com.cynergisuite.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.cynergisuite.middleware.dto.spi.DataTransferObjectBase
import com.cynergisuite.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Digits
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class VerificationLandlord (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val address: Boolean?,
   val altPhone: String?,
   val leaseType: String?,
   val leaveMessage: Boolean?,
   val length: String?,
   val name: String?,
   val paidRent: String?,
   val phone: Boolean?,
   val reliable: Boolean?,
   val rent: BigDecimal?,
   val verification: IdentifiableEntity
) : Entity<VerificationLandlord> {

   constructor(dto: VerificationLandlordDto, verification: IdentifiableEntity) :
      this(
         id = dto.id,
         address = dto.address,
         altPhone = dto.altPhone,
         leaseType = dto.leaseType,
         leaveMessage = dto.leaveMessage,
         length = dto.length,
         name = dto.name,
         paidRent = dto.paidRent,
         phone = dto.phone,
         reliable = dto.reliable,
         rent = dto.rent,
         verification = verification
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): VerificationLandlord  = copy()

   override fun toString(): String {
      return "VerificationLandlord(id=$id, uuRowId=$uuRowId, timeCreated=$timeCreated, timeUpdated=$timeUpdated, address=$address, altPhone=$altPhone, leaseType=$leaseType, leaveMessage=$leaveMessage, length=$length, name=$name, paidRent=$paidRent, phone=$phone, reliable=$reliable, rent=$rent, verification=${verification.entityId()})"
   }
}

@JsonInclude(NON_NULL)
data class VerificationLandlordDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
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

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
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

) : DataTransferObjectBase<VerificationLandlordDto>() {

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

   override fun copyMe(): VerificationLandlordDto = copy()

   override fun dtoId(): Long? = id
}
