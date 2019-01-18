package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Digits
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class VerificationAuto(
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val address: Boolean?,
   val comment: String?,
   val dealerPhone: String?,
   val diffAddress: String?,
   val diffEmployee: String?,
   val diffPhone: String?,
   val dmvVerify: Boolean?,
   val employer: Boolean?,
   val lastPayment: LocalDate?,
   val name: String?,
   val nextPayment: LocalDate?,
   val note: String?,
   val paymentFrequency: String?,
   val payment: BigDecimal?,
   val pendingAction: String?,
   val phone: Boolean?,
   val previousLoan: Boolean?,
   val purchaseDate: LocalDate?,
   val related: String?
) : Entity<VerificationAuto> {
   constructor(dto: VerificationAutoDto) :
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

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): VerificationAuto = copy()
}

data class VerificationAutoDto(

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   var address: Boolean?,

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

   var dmvVerify: Boolean?,

   var employer: Boolean?,

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

   var phone: Boolean?,

   var previousLoan: Boolean?,

   var purchaseDate: LocalDate?,

   @field:Size(max = 50)
   var related: String?

) : DataTransferObjectBase<VerificationAutoDto>() {
   constructor(entity: VerificationAuto) :
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

   override fun copyMe(): VerificationAutoDto = copy()

   override fun dtoId(): Long? = id
}
