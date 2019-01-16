package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Size

data class ChecklistEmployment(
   val id: Long?,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val department: String?,
   val hireDate: LocalDate?,
   val leaveMessage: Boolean?,
   val name: String?,
   val reliable: Boolean?,
   val title: String?
) : Entity {

   constructor(dto: ChecklistEmploymentDto) :
      this(
         id = dto.id,
         department = dto.department,
         hireDate = dto.hireDate,
         leaveMessage = dto.leaveMessage,
         name = dto.name,
         reliable = dto.reliable,
         title = dto.title
      )

   override fun entityId(): Long? = id
}

data class ChecklistEmploymentDto(

   var id: Long? = null,

   @field:Size(max = 50)
   var department: String? = null,

   var hireDate: LocalDate? = null,

   var leaveMessage: Boolean? = null,

   @field:Size(max = 50)
   var name: String? = null,

   var reliable: Boolean?,

   @field:Size(max= 50)
   var title: String? = null

) : DataTransferObjectBase<ChecklistEmploymentDto>() {

   constructor(entity: ChecklistEmployment) :
      this(
         id = entity.id,
         department = entity.department,
         hireDate = entity.hireDate,
         leaveMessage = entity.leaveMessage,
         name = entity.name,
         reliable = entity.reliable,
         title = entity.title
      )

   override fun copyMe(): ChecklistEmploymentDto = copy()
}
