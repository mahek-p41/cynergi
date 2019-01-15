package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.POSITIVE_NUMBER_REQUIRED
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Digits
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class ChecklistAuto(
   var id: Long? = null,
   var uuRowId: UUID? = UUID.randomUUID(),
   var timeCreated: OffsetDateTime? = OffsetDateTime.now(),
   var timeUpdated: OffsetDateTime? = timeCreated,
   var address: Boolean = false,
   var comment: String?,
   var dealerPhone: String?,
   var diffAddress: String?,
   var diffEmployee: String?,
   var diffPhone: String?,
   var dmvVerify: Boolean = false,
   var employer: Boolean = false,
   var lastPayment: LocalDate?,
   var name: String?,
   var nextPayment: LocalDate?,
   var note: String?,
   var paymentFrequency: String?,
   var payment: BigDecimal?,
   var pendingAction: String?,
   var phone: Boolean = false,
   var previousLoan: Boolean = false,
   var purchaseDate: LocalDate?,
   var related: String?

) : Entity {

   constructor(dto: ChecklistAutoDto) :
      this(
         id = dto.id,
         address = dto.address,
         comment = dto.comment,
         dealerPhone = dto.dealerPhone,
         diffAddress = dto.diffAddress,
         diffEmployee = dto.diffEmployee,
         diffPhone = dto.diffPhone,
         dmvVerify = dto.dmvVerify,
         employer = dto.employer,
         lastPayment = dto.lastPayment,
         name = dto.name,
         nextPayment = dto.nextPayment,
         note = dto.note,
         paymentFrequency = dto.paymentFrequency,
         payment = dto.payment,
         pendingAction = dto.pendingAction,
         phone = dto.phone,
         previousLoan = dto.previousLoan,
         purchaseDate = dto.purchaseDate,
         related = dto.related
      )

   override fun entityId(): Long? = id
}

data class ChecklistAutoDto(

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   var address: Boolean = false,

   @field:Size(max = 100)
   var comment: String?,

   @field:Size(max = 18)
   var dealerPhone: String?,

   @field:Size(max = 50)
   var diffAddress: String?,

   @field:Size(max = 50)
   var diffEmployee: String?,

   @field:Size(max = 18)
   var diffPhone: String?,

   var dmvVerify: Boolean = false,

   var employer: Boolean = false,

   var lastPayment: LocalDate?,

   @field:Size(max = 50)
   var name: String?,

   var nextPayment: LocalDate?,

   @field:Size(max = 50)
   var note: String?,

   @field:Size(max = 10)
   var paymentFrequency: String?,

   @field:Digits(integer = 19, fraction = 2)
   var payment: BigDecimal?,

   @field:Size(max = 50)
   var pendingAction: String?,

   var phone: Boolean = false,

   var previousLoan: Boolean = false,

   var purchaseDate: LocalDate?,

   @field:Size(max = 50)
   var related: String?

) : DataTransferObjectBase<ChecklistAutoDto>() {
   constructor(entity: ChecklistAuto) :
      this(
         id = entity.id,
         address = entity.address,
         comment = entity.comment,
         dealerPhone = entity.dealerPhone,
         diffAddress = entity.diffAddress,
         diffEmployee = entity.diffEmployee,
         diffPhone = entity.diffPhone,
         dmvVerify = entity.dmvVerify,
         employer = entity.employer,
         lastPayment = entity.lastPayment,
         name = entity.name,
         nextPayment = entity.nextPayment,
         note = entity.note,
         paymentFrequency = entity.paymentFrequency,
         payment = entity.payment,
         pendingAction = entity.pendingAction,
         phone = entity.phone,
         previousLoan = entity.previousLoan,
         purchaseDate = entity.purchaseDate,
         related = entity.related
      )

   override fun copyMe(): ChecklistAutoDto = copy()
}
