package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto
import com.hightouchinc.cynergi.middleware.dto.helper.SimpleIdentifiableDto
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.entity.helper.SimpleIdentifiableEntity
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

data class CompanyModuleAccess (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val level: Int,
   val company: IdentifiableEntity,
   val module: IdentifiableEntity
) : Entity<CompanyModuleAccess> {

   constructor(level: Int, company: IdentifiableEntity, module: IdentifiableEntity) :
      this(
         id = null,
         level = level,
         company = company,
         module = module
      )

   constructor(dto: CompanyModuleAccessDto, company: IdentifiableDto, module: IdentifiableDto) :
      this(
         id = dto.id,
         level = dto.level!!,
         company = SimpleIdentifiableEntity(identifiableDto = company),
         module = SimpleIdentifiableEntity(identifiableDto = module)
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): CompanyModuleAccess = copy()
}

@JsonInclude(NON_NULL)
data class CompanyModuleAccessDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:NotNull(message = NOT_NULL)
   @field:Min(1, message = MIN)
   @field:Max(99, message = MAX)
   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var level: Int? = null,

   @field:NotNull(message = NOT_NULL)
   var company: IdentifiableDto? = null,

   @field:NotNull(message = NOT_NULL)
   var module: IdentifiableDto? = null

) : DataTransferObjectBase<CompanyModuleAccessDto>() {

   constructor(entity: CompanyModuleAccess) :
      this(
         id = entity.id,
         level = entity.level,
         company = SimpleIdentifiableDto(identifiableEntity = entity.company),
         module = SimpleIdentifiableDto(identifiableEntity = entity.module)
      )

   override fun dtoId(): Long? = id
   override fun copyMe(): CompanyModuleAccessDto = copy()
}
