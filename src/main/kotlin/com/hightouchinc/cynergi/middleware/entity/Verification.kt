package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.NOT_NULL
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.SIZE
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.util.UUID
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class Verification(
   val id: Long?,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val customerAccount: String, // TODO convert from soft foreign key to customer
   val customerComments: String?,
   val verifiedBy: String, // TODO convert from soft foreign key to employee
   val verifiedTime: OffsetDateTime,
   val company: String, // TODO convert from soft foreign key to point to a company, does this even need to exist since you'd be able to walk the customer_account back up to get the company
   var auto: VerificationAuto? = null,
   var employment: VerificationEmployment? = null,
   var landlord: VerificationLandlord? = null,
   val references: MutableList<VerificationReference> = mutableListOf()
) : Entity<Verification> {
   constructor(dto: VerificationDto, company: String) :
      this(
         id = dto.id,
         customerAccount = dto.customerAccount!!,
         customerComments = dto.customerComments,
         verifiedBy = dto.verifiedBy!!,
         verifiedTime = dto.verifiedTime!!.atStartOfDay(ZoneId.of("UTC")).toOffsetDateTime(),
         company = company
      ) {

      this.auto = copyAutoDtoToEntity(dto = dto, parent = this)
      this.employment = copyEmploymentDtoToEntity(dto = dto, parent = this)
      this.landlord = copyLandlordDtoToEntity(dto = dto, parent = this)

      dto.references.asSequence().map { VerificationReference(it, this) }.forEach { this.references.add(it) }
   }

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): Verification = copy()
}

@JsonInclude(NON_NULL)
data class VerificationDto(
   var id: Long?,

   @field:Size(max = 10, message = SIZE)
   @field:NotNull(message = NOT_NULL)
   @field:JsonProperty("cust_acct")
   var customerAccount: String?,

   @field:Size(max = 255, message = SIZE)
   @field:JsonProperty("cust_comments")
   var customerComments: String?,

   @field:Size(max = 50, message = SIZE)
   @field:NotNull(message = NOT_NULL)
   @field:JsonProperty("cust_verified_by")
   var verifiedBy: String?,

   @field:NotNull(message = NOT_NULL)
   @field:JsonProperty("cust_verified_date")
   var verifiedTime: LocalDate?,

   @field:Nullable
   @field:JsonProperty("checklist_auto")
   var auto: VerificationAutoDto?,

   @field:Nullable
   @field:JsonProperty("checklist_employment")
   var employment: VerificationEmploymentDto?,

   @field:Nullable
   @field:JsonProperty("checklist_landlord")
   var landlord: VerificationLandlordDto?,

   @field:Nullable
   @field:Size(max = 6)
   @field:JsonDeserialize(contentAs = VerificationReferenceDto::class)
   @field:JsonProperty("checklist_references")
   val references: MutableList<VerificationReferenceDto> = mutableListOf()

) : DataTransferObjectBase<VerificationDto>() {
   constructor(entity: Verification) :
      this(
         id = entity.id,
         customerAccount = entity.customerAccount,
         customerComments = entity.customerComments,
         verifiedBy = entity.verifiedBy,
         verifiedTime = entity.verifiedTime.toLocalDate(),
         auto = copyAutoEntityToDto(entity = entity),
         employment = copyEmploymentEntityToDto(entity = entity),
         landlord = copyLandlordEntityToDto(entity = entity),
         references = entity.references.asSequence().map { VerificationReferenceDto(it) }.sortedBy { it.id }.toMutableList()
      )

   override fun copyMe(): VerificationDto = this.copy()

   override fun dtoId(): Long? = id
}

/*
 * the functions defined below are placed here since they cannot be placed on the data classes themselves. They are
 * listed as private so that they should be hidden from code outside of this file as they are an implementation detail
 * of the way the verification data associations are managed.
 */

private fun copyAutoDtoToEntity(dto: VerificationDto, parent: Verification): VerificationAuto? {
   val auto = dto.auto

   return if (auto != null) {
      VerificationAuto(dto = auto, verification = parent)
   } else {
      null
   }
}

private fun copyAutoEntityToDto(entity: Verification): VerificationAutoDto? {
   val auto = entity.auto

   return if (auto != null) {
      VerificationAutoDto(entity = auto)
   } else {
      null
   }
}

private fun copyEmploymentDtoToEntity(dto: VerificationDto, parent: Verification): VerificationEmployment? {
   val employment = dto.employment

   return if (employment != null) {
      VerificationEmployment(dto = employment, verification = parent)
   } else {
      null
   }
}

private fun copyEmploymentEntityToDto(entity: Verification): VerificationEmploymentDto? {
   val employment = entity.employment

   return if (employment != null) {
      VerificationEmploymentDto(entity = employment)
   } else {
      null
   }
}

private fun copyLandlordDtoToEntity(dto: VerificationDto, parent: Verification): VerificationLandlord? {
   val landlord = dto.landlord

   return if (landlord != null) {
      VerificationLandlord(dto = landlord, verification = parent)
   } else {
      return null
   }
}

private fun copyLandlordEntityToDto(entity: Verification): VerificationLandlordDto? {
   val landlord = entity.landlord

   return if (landlord != null) {
      VerificationLandlordDto(entity = landlord)
   } else {
      null
   }
}
