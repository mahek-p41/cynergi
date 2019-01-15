package com.hightouchinc.cynergi.middleware.entity

import com.hightouchinc.cynergi.middleware.entity.spi.DataTransferObjectBase
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Size

data class ChecklistEmployment(
   var id: Long?,
   var uuRowId: UUID = UUID.randomUUID(),
   var timeCreated: OffsetDateTime = OffsetDateTime.now(),
   var timeUpdated: OffsetDateTime = timeCreated,
   var department: String?,
   var hireDate: LocalDate?,
   var leaveMessage: Boolean = false,
   var name: String?,
   var reliable: Boolean = false,
   var title: String?
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

   var leaveMessage: Boolean = false,

   @field:Size(max = 50)
   var name: String? = null,

   var reliable: Boolean = false,

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
