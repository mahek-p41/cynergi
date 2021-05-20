package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.LegacyIdentifiable
import java.time.LocalDate
import java.time.OffsetDateTime

data class Verification(
   val id: Long? = null,
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val customerAccount: String, // TODO convert from soft foreign key to customer
   val customerComments: String?,
   val verifiedBy: String, // TODO convert from soft foreign key to employee
   val verifiedTime: LocalDate,
   val company: String, // TODO convert from soft foreign key to point to a company, does this even need to exist since you'd be able to walk the customer_account back up to get the company
   var auto: VerificationAuto? = null,
   var employment: VerificationEmployment? = null,
   var landlord: VerificationLandlord? = null,
   val references: MutableList<VerificationReference> = mutableListOf()
) : LegacyIdentifiable {

   constructor(dto: VerificationValueObject, company: String) :
      this(
         id = dto.id,
         customerAccount = dto.customerAccount!!,
         customerComments = dto.customerComments,
         verifiedBy = dto.verifiedBy!!,
         verifiedTime = dto.verifiedTime!!,
         company = company
      ) {

         this.auto = copyAutoDtoToEntity(dto = dto, parent = this)
         this.employment = copyEmploymentDtoToEntity(dto = dto, parent = this)
         this.landlord = copyLandlordDtoToEntity(dto = dto, parent = this)

         dto.references.asSequence().map { VerificationReference(it, this) }.forEach { this.references.add(it) }
      }

   override fun myId(): Long? = id

   fun copyMe(): Verification = copy()
}

private fun copyAutoDtoToEntity(dto: VerificationValueObject, parent: Verification): VerificationAuto? {
   val auto = dto.auto

   return if (auto != null) {
      VerificationAuto(dto = auto, verification = parent)
   } else {
      null
   }
}

private fun copyEmploymentDtoToEntity(dto: VerificationValueObject, parent: Verification): VerificationEmployment? {
   val employment = dto.employment

   return if (employment != null) {
      VerificationEmployment(dto = employment, verification = parent)
   } else {
      null
   }
}

private fun copyLandlordDtoToEntity(dto: VerificationValueObject, parent: Verification): VerificationLandlord? {
   val landlord = dto.landlord

   return if (landlord != null) {
      VerificationLandlord(dto = landlord, verification = parent)
   } else {
      return null
   }
}
