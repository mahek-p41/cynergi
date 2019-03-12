package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.dto.IdentifiableDto
import com.hightouchinc.cynergi.middleware.dto.helper.SimpleIdentifiableDto
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

data class Company (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val name: String,
   val organization: IdentifiableEntity
) : Entity<Company> {

   constructor(name: String, organization: Organization) :
      this(
         id = null,
         name = name,
         organization = organization
      )

   constructor(dto: CompanyDto) :
      this(
         id = dto.id,
         name = dto.name!!,
         organization = SimpleIdentifiableEntity(dto.organization!!)
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Company = copy()
}

@JsonInclude(NON_NULL)
data class CompanyDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:NotNull(message = NOT_NULL)
   @field:Size(message = SIZE, min = 6, max = 6)
   var name: String? = null,

   @field:NotNull(message = NOT_NULL)
   var organization: IdentifiableDto? = null

) : DataTransferObjectBase<CompanyDto>() {

   constructor(entity: Company) :
      this(
         id = entity.id,
         name = entity.name,
         organization = SimpleIdentifiableDto(entity.organization)
      )

   override fun dtoId(): Long? = id
   override fun copyMe(): CompanyDto = copy()
}
