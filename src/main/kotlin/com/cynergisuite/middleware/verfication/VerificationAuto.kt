package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.Identifiable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

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
   val related: String?,
   val verification: Identifiable
) : Identifiable {

   constructor(dto: VerificationAutoValueObject, verification: Identifiable) :
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
         related = dto.related,
         verification = verification
      )

   override fun myId(): Long? = id

   override fun toString(): String {
      return "VerificationAuto(id=$id, uuRowId=$uuRowId, timeCreated=$timeCreated, timeUpdated=$timeUpdated, address=$address, comment=$comment, dealerPhone=$dealerPhone, diffAddress=$diffAddress, diffEmployee=$diffEmployee, diffPhone=$diffPhone, dmvVerify=$dmvVerify, employer=$employer, lastPayment=$lastPayment, name=$name, nextPayment=$nextPayment, note=$note, paymentFrequency=$paymentFrequency, payment=$payment, pendingAction=$pendingAction, phone=$phone, previousLoan=$previousLoan, purchaseDate=$purchaseDate, related=$related, verification=${verification.myId()})"
   }
}
