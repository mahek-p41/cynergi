package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.NOT_NULL
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.SIZE
import java.time.LocalDateTime
import java.util.UUID
import javax.annotation.Nullable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class Checklist(
   var id: Long?,
   var uuRowId: UUID = UUID.randomUUID(),
   var timeCreated: LocalDateTime = LocalDateTime.now(),
   var timeUpdated: LocalDateTime = timeCreated,
   var customerAccount: String, // TODO convert from soft foreign key to soft_customer
   var customerComments: String,
   var verifiedBy: String, // TODO convert from soft foreign key to soft_employee
   var verifiedTime: LocalDateTime,
   var company: String, // TODO convert from soft foreign key to point to a soft_company
   var auto: ChecklistAuto?

): Entity {
   constructor(dto: ChecklistDto, company: String):
      this(
         id = dto.id,
         customerAccount = dto.customerAccount,
         customerComments = dto.customerComments,
         verifiedBy = dto.verifiedBy,
         verifiedTime = dto.verifiedTime,
         company = company,
         auto = copyAutoDtoToEntity(dto = dto)
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
   var verifiedTime: LocalDateTime,

   @field:Nullable
   @field:JsonProperty("checklist_auto")
   var auto: ChecklistAutoDto?

): DataTransferObjectBase<ChecklistDto>() {
   constructor(entity: Checklist):
      this(
         id = entity.id,
         customerAccount = entity.customerAccount,
         customerComments = entity.customerComments,
         verifiedBy = entity.verifiedBy,
         verifiedTime = entity.verifiedTime,
         auto = copyAutoEntityToDto(entity = entity)
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
