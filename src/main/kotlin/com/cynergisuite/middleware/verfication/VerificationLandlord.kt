package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.Entity
import com.cynergisuite.domain.Identifiable
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

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
   val verification: Identifiable
) : Entity<VerificationLandlord> {

   constructor(dto: VerificationLandlordValueObject, verification: Identifiable) :
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

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): VerificationLandlord = copy()

   override fun toString(): String {
      return "VerificationLandlord(id=$id, uuRowId=$uuRowId, timeCreated=$timeCreated, timeUpdated=$timeUpdated, address=$address, altPhone=$altPhone, leaseType=$leaseType, leaveMessage=$leaveMessage, length=$length, name=$name, paidRent=$paidRent, phone=$phone, reliable=$reliable, rent=$rent, verification=${verification.myId()})"
   }
}
