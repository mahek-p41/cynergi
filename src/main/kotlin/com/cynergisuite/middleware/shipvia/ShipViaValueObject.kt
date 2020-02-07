package com.cynergisuite.middleware.shipvia

import com.cynergisuite.domain.ValueObjectBase
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
data class ShipViaValueObject(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Size(min = 3, max = 30)
   var description: String?

) : ValueObjectBase<ShipViaValueObject>() {
   constructor(description: String?) :
      this(
         id = null,
         description = description
      )

   constructor(entity: ShipViaEntity) :
      this(
         id = entity.id,
         description = entity.description
      )

   override fun myId(): Long? = id
   override fun copyMe(): ShipViaValueObject = copy()
}
