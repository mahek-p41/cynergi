package com.cynergisuite.middleware.verfication

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "Verification", title = "Customer Verification", description = "A single verification for a single customer")
data class VerificationValueObject(
   var id: Long?,

   @field:Size(max = 10)
   @field:NotNull
   @field:JsonProperty("cust_acct")
   var customerAccount: String?,

   @field:Size(max = 255)
   @field:JsonProperty("cust_comments")
   var customerComments: String?,

   @field:Size(max = 50)
   @field:NotNull
   @field:JsonProperty("cust_verified_by")
   var verifiedBy: String?,

   @field:NotNull
   @field:JsonProperty("cust_verified_date")
   var verifiedTime: LocalDate?,

   @field:Nullable
   @field:JsonProperty("checklist_auto")
   var auto: VerificationAutoValueObject?,

   @field:Nullable
   @field:JsonProperty("checklist_employment")
   var employment: VerificationEmploymentValueObject?,

   @field:Nullable
   @field:JsonProperty("checklist_landlord")
   var landlord: VerificationLandlordValueObject?,

   @field:Nullable
   @field:Size(max = 6)
   @field:JsonDeserialize(contentAs = VerificationReferenceValueObject::class)
   @field:JsonProperty("checklist_references")
   var references: MutableList<VerificationReferenceValueObject> = mutableListOf()

) : ValueObjectBase<VerificationValueObject>() {
   constructor(entity: Verification) :
      this(
         id = entity.id,
         customerAccount = entity.customerAccount,
         customerComments = entity.customerComments,
         verifiedBy = entity.verifiedBy,
         verifiedTime = entity.verifiedTime.toLocalDate(),
         auto = copyAutoEntityToValueObject(entity = entity),
         employment = copyEmploymentEntityToValueObject(entity = entity),
         landlord = copyLandlordEntityToValueObject(entity = entity),
         references = entity.references.asSequence().map { VerificationReferenceValueObject(it) }.sortedBy { it.id }.toMutableList()
      )

   override fun copyMe(): VerificationValueObject = this.copy()

   override fun myId(): Long? = id
}

private fun copyAutoEntityToValueObject(entity: Verification): VerificationAutoValueObject? {
   val auto = entity.auto

   return if (auto != null) {
      VerificationAutoValueObject(entity = auto)
   } else {
      null
   }
}

private fun copyEmploymentEntityToValueObject(entity: Verification): VerificationEmploymentValueObject? {
   val employment = entity.employment

   return if (employment != null) {
      VerificationEmploymentValueObject(entity = employment)
   } else {
      null
   }
}

private fun copyLandlordEntityToValueObject(entity: Verification): VerificationLandlordValueObject? {
   val landlord = entity.landlord

   return if (landlord != null) {
      VerificationLandlordValueObject(entity = landlord)
   } else {
      null
   }
}
