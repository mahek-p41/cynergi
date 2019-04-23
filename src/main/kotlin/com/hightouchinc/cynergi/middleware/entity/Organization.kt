package com.hightouchinc.cynergi.middleware.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.hightouchinc.cynergi.middleware.dto.spi.DataTransferObjectBase
import com.hightouchinc.cynergi.middleware.localization.MessageCodes
import com.hightouchinc.cynergi.middleware.localization.MessageCodes.Cynergi.POSITIVE_NUMBER_REQUIRED
import java.time.OffsetDateTime
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

data class Organization (
   val id: Long? = null,
   val uuRowId: UUID = UUID.randomUUID(),
   val timeCreated: OffsetDateTime = OffsetDateTime.now(),
   val timeUpdated: OffsetDateTime = timeCreated,
   val name: String,
   val billingAccount: String
) : Entity<Organization> {

   constructor(name: String, billingAccount: String) :
      this(
         id = null,
         name = name,
         billingAccount = billingAccount
      )

   constructor(dto: OrganizationDto) :
      this(
         id = dto.id,
         name = dto.name!!,
         billingAccount = dto.billingAccount!!
      )

   override fun entityId(): Long? = id
   override fun rowId(): UUID = uuRowId
   override fun copyMe(): Organization = copy()
}

@JsonInclude(NON_NULL)
data class OrganizationDto (

   @field:Positive(message = POSITIVE_NUMBER_REQUIRED)
   var id: Long? = null,

   @field:NotNull(message = MessageCodes.Validation.NOT_NULL)
   @field:Size(message = MessageCodes.Validation.SIZE, min = 6, max = 6)
   var name: String? = null,

   @field:NotNull(message = MessageCodes.Validation.NOT_NULL)
   @field:Size(message = MessageCodes.Validation.SIZE, min = 1, max = 50) // TODO figure out what the max and min values are for this
   var billingAccount: String? = null

) : DataTransferObjectBase<OrganizationDto>() {

   constructor(entity: Organization) :
      this(
         id = entity.id,
         name = entity.name,
         billingAccount = entity.billingAccount
      )

   override fun dtoId(): Long? = id
   override fun copyMe(): OrganizationDto = copy()
}
