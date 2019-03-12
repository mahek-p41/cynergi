package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Validation.MAX
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Validation.MIN
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Validation.NOT_NULL
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class Department (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val name: String,
   val level: Int
) : Entity<Department> {

   constructor(name: String, level: Int) :
      this(
         id = null,
         name = name,
         level = level
      )

   constructor(dto: DepartmentDto) :
      this(
         id = dto.id,
         name = dto.name!!,
         level = dto.level!!
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Department = copy()
}

@JsonInclude(NON_NULL)
data class DepartmentDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:NotNull(message = NOT_NULL)
   var name: String? = null,

   @field:NotNull(message = NOT_NULL)
   @field:Min(1, message = MIN)
   @field:Max(99, message = MAX)
   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var level: Int? = null

) : DataTransferObjectBase<DepartmentDto>() {

   constructor(entity: Department) :
      this(
         id = entity.id,
         name = entity.name,
         level = entity.level
      )

   override fun dtoId(): Long? = id
   override fun copyMe(): DepartmentDto = copy()
}
