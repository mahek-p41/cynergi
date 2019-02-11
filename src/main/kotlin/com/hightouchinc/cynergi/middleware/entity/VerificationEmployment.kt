package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.annotation.JsonProperty
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Size

data class VerificationEmployment(
   val id: Long?,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val department: String?,
   val hireDate: LocalDate?,
   val leaveMessage: Boolean?,
   val name: String?,
   val reliable: Boolean?,
   val title: String?,
   val verification: IdentifiableEntity
) : Entity<VerificationEmployment> {
   constructor(dto: VerificationEmploymentDto, verification: IdentifiableEntity) :
      this(
         id = dto.id,
         department = dto.department,
         hireDate = dto.hireDate,
         leaveMessage = dto.leaveMessage,
         name = dto.name,
         reliable = dto.reliable,
         title = dto.title,
         verification = verification
      )

   override fun entityId(): Long? = id

   override fun rowId(): UUID = uuRowId

   override fun copyMe(): VerificationEmployment = copy()

   override fun toString(): String {
      return "VerificationEmployment(id=$id, uuRowId=$uuRowId, timeCreated=$timeCreated, timeUpdated=$timeUpdated, department=$department, hireDate=$hireDate, leaveMessage=$leaveMessage, name=$name, reliable=$reliable, title=$title, verification=${verification.entityId()})"
   }
}

@JsonInclude(NON_NULL)
data class VerificationEmploymentDto(

   var id: Long? = null,

   @field:Size(max = 50)
   @field:JsonProperty("emp_dept")
   var department: String? = null,

   @field:JsonProperty("emp_hire_date")
   var hireDate: LocalDate? = null,

   @field:JsonProperty("emp_leave_msg")
   var leaveMessage: Boolean? = null,

   @field:Size(max = 50)
   @field:JsonProperty("emp_name")
   var name: String? = null,

   @field:JsonProperty("emp_reliable")
   var reliable: Boolean?,

   @field:Size(max= 50)
   @field:JsonProperty("emp_title")
   var title: String? = null

) : DataTransferObjectBase<VerificationEmploymentDto>() {

   constructor(entity: VerificationEmployment) :
      this(
         id = entity.id,
         department = entity.department,
         hireDate = entity.hireDate,
         leaveMessage = entity.leaveMessage,
         name = entity.name,
         reliable = entity.reliable,
         title = entity.title
      )

   override fun copyMe(): VerificationEmploymentDto = copy()

   override fun dtoId(): Long? = id
}
