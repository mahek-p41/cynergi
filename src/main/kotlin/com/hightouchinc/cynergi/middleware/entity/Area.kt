package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto
import com.hightouchinc.cynergi.middleware.dto.helper.SimpleIdentifiableDto
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Validation.NOT_NULL
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class Area (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val company: IdentifiableEntity,
   val menu: IdentifiableEntity,
   val level: Int
) : Entity<Area> {

   constructor(menu: IdentifiableEntity, company: IdentifiableEntity, level: Int) :
      this(
         id = null,
         company = company,
         menu = menu,
         level = level
      )

   constructor(dto: AreaDto, companyId: Long) :
      this(
         id = dto.id,
         company = SimpleIdentifiableEntity(id = companyId),
         menu = SimpleIdentifiableEntity(dto.menu!!),
         level = dto.level!!
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Area = copy()
}

@JsonInclude(NON_NULL)
data class AreaDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:NotNull(message = NOT_NULL)
   var menu: IdentifiableDto?,

   @field:NotNull(message = NOT_NULL)
   @field:Min(1)
   @field:Max(99)
   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var level: Int?

) : DataTransferObjectBase<AreaDto>() {

   constructor(entity: Area) :
      this(
         id = entity.id,
         menu = SimpleIdentifiableDto(entity.menu),
         level = entity.level
      )

   override fun dtoId(): Long? = id
   override fun copyMe(): AreaDto = copy()
}
