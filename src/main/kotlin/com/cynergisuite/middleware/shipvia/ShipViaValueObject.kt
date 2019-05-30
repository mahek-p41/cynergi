package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.ValueObjectBase
import com.cynergisuite.middleware.localization.MessageCodes.Validation.NOT_NULL
import com.cynergisuite.middleware.localization.MessageCodes.Validation.POSITIVE
import com.cynergisuite.middleware.localization.MessageCodes.Validation.SIZE
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class ShipViaValueObject(
   @field:Positive(message = POSITIVE)
   var id: Long? = null,

   @field:NotNull(message = NOT_NULL)
   @field:Size(min = 3, max = 255, message = SIZE)
   val shipViaName: String?,

   @field:NotNull(message = NOT_NULL)
   @field:Size(min = 3, max = 500, message = SIZE)
   val shipViaDescription: String?

) : ValueObjectBase<ShipViaValueObject>() {
   constructor(shipViaName: String, shipViaDescription: String) :
      this(
         id = null,
         shipViaName = shipViaName,
         shipViaDescription = shipViaDescription
      )

   constructor(entity: ShipVia) :
      this(
         id = null,
         shipViaName = entity.shipViaName,
         shipViaDescription = entity.shipViaDescription
      )

   override fun dtoId(): Long? = id
   override fun copyMe(): ShipViaValueObject = copy()
}
