package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.NOT_NULL
import com.hightouchinc.cynergi.middleware.validator.ErrorCodes.Validation.SIZE
import java.time.LocalDateTime
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

data class Checklist(
   var id: Long?,
   var uuRowId: UUID,
   var timeCreated: LocalDateTime,
   var timeUpdated: LocalDateTime,
   var customerAccount: String, // TODO convert from soft foreign key to customer
   var customerComments: String,
   var verifiedBy: String, // TODO convert from soft foreign key to employee
   var verifiedTime: LocalDateTime,
   var company: String // TODO convert from soft foreign key to point to a company

): Entity {
   constructor(dto: ChecklistDto, company: String):
      this(
         id = dto.id,
         uuRowId = UUID.randomUUID(),
         timeCreated = LocalDateTime.now(),
         timeUpdated = LocalDateTime.now(),
         customerAccount = dto.customerAccount,
         customerComments = dto.customerComments,
         verifiedBy = dto.verifiedBy,
         verifiedTime = dto.verifiedTime,
         company = company
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
   var verifiedTime: LocalDateTime

): DataTransferObjectBase<ChecklistDto>() {
   constructor(checklist: Checklist):
      this(
         id = checklist.id,
         customerAccount = checklist.customerAccount,
         customerComments = checklist.customerComments,
         verifiedBy = checklist.verifiedBy,
         verifiedTime = checklist.verifiedTime
      )

   override fun copyMe(): ChecklistDto {
      return this.copy()
   }
}
