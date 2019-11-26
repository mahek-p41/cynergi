package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class ShipViaValueObject(
   @field:Positive
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 3, max = 255)
   var name: String?,

   @field:NotNull
   @field:Size(min = 3, max = 500)
   var description: String?

) : ValueObjectBase<ShipViaValueObject>() {
   constructor(name: String, description: String) :
      this(
         id = null,
         name = name,
         description = description
      )

   constructor(entity: ShipVia) :
      this(
         id = entity.id,
         name = entity.name,
         description = entity.description
      )

   override fun myId(): Long? = id
   override fun copyMe(): ShipViaValueObject = copy()
}
