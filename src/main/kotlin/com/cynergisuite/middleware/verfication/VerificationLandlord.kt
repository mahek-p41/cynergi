package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.LegacyIdentifiable
import java.math.BigDecimal
import java.time.OffsetDateTime

data class VerificationLandlord(
   val id: Long? = null,
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
   val verification: LegacyIdentifiable
) : LegacyIdentifiable {

   constructor(dto: VerificationLandlordValueObject, verification: LegacyIdentifiable) :
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

   override fun myId(): Long? = id

   override fun toString(): String {
      return "VerificationLandlord(id=$id, timeCreated=$timeCreated, timeUpdated=$timeUpdated, address=$address, altPhone=$altPhone, leaseType=$leaseType, leaveMessage=$leaveMessage, length=$length, name=$name, paidRent=$paidRent, phone=$phone, reliable=$reliable, rent=$rent, verification=${verification.myId()})"
   }
}
