package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.NOT_NULL
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.SIZE
import java.time.OffsetDateTime
import java.util.UUID
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class Checklist(
   var id: Long?,
   var uuRowId: UUID = UUID.randomUUID(),
   var timeCreated: OffsetDateTime = OffsetDateTime.now(),
   var timeUpdated: OffsetDateTime = timeCreated,
   var customerAccount: String, // TODO convert from soft foreign key to soft_customer
   var customerComments: String,
   var verifiedBy: String, // TODO convert from soft foreign key to soft_employee
   var verifiedTime: OffsetDateTime,
   var company: String, // TODO convert from soft foreign key to point to a soft_company, does this even need to exist since you'd be able to walk the customer_account back up to get the company
   var auto: ChecklistAuto?,
   var employment: ChecklistEmployment?

) : Entity {
   constructor(dto: ChecklistDto, company: String) :
      this(
         id = dto.id,
         customerAccount = dto.customerAccount,
         customerComments = dto.customerComments,
         verifiedBy = dto.verifiedBy,
         verifiedTime = dto.verifiedTime,
         company = company,
         auto = copyAutoDtoToEntity(dto = dto),
         employment = copyEmploymentDtoToEntity(dto = dto)
      )

   override fun entityId(): Long? = id
}

data class ChecklistDto(
   var id: Long?,

   @field:Size(max = 10, message = SIZE)
   @field:NotNull(message = NOT_NULL)
   @field:JsonProperty("cust_acct")
   var customerAccount: String,

   @field:Size(max = 255, message = SIZE)
   @field:JsonProperty("cust_comments")
   var customerComments: String,

   @field:Size(max = 50, message = SIZE)
   @field:NotNull(message = NOT_NULL)
   @field:JsonProperty("cust_verified_by")
   var verifiedBy: String,

   @field:NotNull(message = NOT_NULL)
   @field:JsonProperty("cust_verified_date")
   var verifiedTime: OffsetDateTime,

   @field:Nullable
   @field:JsonProperty("checklist_auto")
   var auto: ChecklistAutoDto?,

   @field:Nullable
   @field:JsonProperty("checklist_employment")
   var employment: ChecklistEmploymentDto?

) : DataTransferObjectBase<ChecklistDto>() {
   constructor(entity: Checklist) :
      this(
         id = entity.id,
         customerAccount = entity.customerAccount,
         customerComments = entity.customerComments,
         verifiedBy = entity.verifiedBy,
         verifiedTime = entity.verifiedTime,
         auto = copyAutoEntityToDto(entity = entity),
         employment = copyEmploymentEntityToDto(entity = entity)
      )

   override fun copyMe(): ChecklistDto {
      return this.copy()
   }
}

private fun copyAutoDtoToEntity(dto: ChecklistDto): ChecklistAuto? {
   val auto = dto.auto

   return if (auto != null) {
      ChecklistAuto(dto = auto)
   } else {
      null
   }
}

private fun copyAutoEntityToDto(entity: Checklist): ChecklistAutoDto? {
   val auto = entity.auto

   return if (auto != null) {
      ChecklistAutoDto(entity = auto)
   } else {
      null
   }
}

private fun copyEmploymentDtoToEntity(dto: ChecklistDto): ChecklistEmployment? {
   val employment = dto.employment

   return if (employment != null) {
      ChecklistEmployment(dto = employment)
   } else {
      null
   }
}

private fun copyEmploymentEntityToDto(entity: Checklist): ChecklistEmploymentDto? {
   val employment = entity.employment

   return if (employment != null) {
      ChecklistEmploymentDto(entity = employment)
   } else {
      null
   }
}
