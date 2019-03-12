package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto
import com.hightouchinc.cynergi.middleware.dto.helper.SimpleIdentifiableDto
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import com.hightouchinc.cynergi.middleware.localization.MessageCodes
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Validation.NOT_NULL
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class Employee (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val userId: String,
   val password: String,
   val firstName: String,
   val lastName: String,
   val department: IdentifiableEntity,
   val company: IdentifiableEntity
) : Entity<Employee> {

   constructor(userId: String, password: String, firstName: String, lastName: String, department: IdentifiableEntity, company: IdentifiableEntity) :
      this(
         id = null,
         userId = userId,
         password = password,
         firstName = firstName,
         lastName = lastName,
         department = department,
         company = company
      )

   constructor(dto: EmployeeDto, company: IdentifiableDto, department: IdentifiableDto) :
      this(
         id = dto.id,
         userId = dto.userId!!,
         password = dto.password!!,
         firstName = dto.firstName!!,
         lastName = dto.lastName!!,
         company = SimpleIdentifiableEntity(identifiableDto = company),
         department = SimpleIdentifiableEntity(identifiableDto = department)
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Employee = copy()
}

@JsonInclude(NON_NULL)
data class EmployeeDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:NotNull(message = NOT_NULL)
   @field:Size(min = 1, max = 8, message = MessageCodes.Validation.SIZE)
   val userId: String?,

   @field:NotNull(message = NOT_NULL)
   @field:Size(min = 1, max = 8, message = MessageCodes.Validation.SIZE)
   val password: String?,

   @field:NotNull(message = NOT_NULL)
   val firstName: String?,

   @field:NotNull(message = NOT_NULL)
   val lastName: String?,

   @field:NotNull(message = NOT_NULL)
   val department: IdentifiableDto?

) : DataTransferObjectBase<EmployeeDto>() {

   constructor(entity: Employee, department: Department) :
      this(
         id = entity.id,
         userId = entity.userId,
         password = entity.password,
         firstName = entity.firstName,
         lastName = entity.lastName,
         department = SimpleIdentifiableDto(identifiableEntity = department)
      )

   override fun dtoId(): Long? = id
   override fun copyMe(): EmployeeDto = copy()
}
