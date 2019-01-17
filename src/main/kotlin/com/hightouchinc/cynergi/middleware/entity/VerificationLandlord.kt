package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
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
   val length: Int?,
   val name: String?,
   val paidRent: String?,
   val phone: Boolean?,
   val reliable: Boolean?,
   val rent: BigDecimal
) : Entity {

   constructor(dto: VerificationLandlordDto) :
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
         rent = dto.rent
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId
}

data class VerificationLandlordDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   var address: Boolean? = null,

   @field:Size(max = 18)
   var altPhone: String?,

   @field:Size(max = 25)
   var leaseType: String?,

   var leaveMessage: Boolean?,

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var length: Int?,

   @field:Size(max = 50)
   var name: String?,

   @field:Size(max = 15)
   var paidRent: String?,

   var phone: Boolean?,

   var reliable: Boolean?,

   @field:Digits(integer = 19, fraction = 2)
   var rent: BigDecimal

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
