package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Validation.NOT_NULL
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Validation.SIZE
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class Module (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val name: String,
   val literal: String,
   val menu: IdentifiableEntity
) : Entity<Module> {

   constructor(name: String, literal: String, menu: IdentifiableEntity) :
      this(
         id = null,
         name = name,
         literal = literal,
         menu = SimpleIdentifiableEntity(menu)
      )

   constructor(dto: ModuleDto) :
      this(
         id = dto.id,
         name = dto.name!!,
         literal = dto.literal!!,
         menu = SimpleIdentifiableEntity(identifiableDto = dto.menu!!)
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Module = copy()
}

@JsonInclude(NON_NULL)
data class ModuleDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:NotNull(message = NOT_NULL)
   @field:Size(message = SIZE, min = 6, max = 6)
   var name: String? = null,

   @field:NotNull(message = NOT_NULL)
   @field:Size(message = SIZE, min = 6, max = 6)
   var literal: String? = null,

   @field:NotNull(message = NOT_NULL)
   @field:Size(message = SIZE, min = 6, max = 6)
   var menu: IdentifiableDto? = null

) : DataTransferObjectBase<ModuleDto>() {

   constructor(entity: Module) :
      this(
         id = entity.id,
         name = entity.name,
         literal = entity.literal
      )

   override fun dtoId(): Long? = id
   override fun copyMe(): ModuleDto = copy()
}
