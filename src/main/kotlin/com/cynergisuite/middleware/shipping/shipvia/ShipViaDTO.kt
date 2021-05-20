package com.cynergisuite.middleware.shipping.shipvia

import com.cynergisuite.domain.Identifiable
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(NON_NULL)
@Schema(name = "ShipVia", title = "Ship Via Definition", description = "Defines a single Ship Via for a company")
data class ShipViaDTO(

   @field:Schema(name = "id", required = false, description = "System generated ID")
   var id: UUID? = null,

   @field:NotNull
   @field:Size(min = 3, max = 30)
   @field:Schema(name = "description", minimum = "1", maximum = "30", description = "Describes the Ship Via")
   var description: String? = null,

   @field:Positive
   @field:Schema(name = "number", minimum = "1", required = false, description = "Ship Via Number")
   var number: Int? = null

) : Identifiable {
   constructor(description: String?, number: Int?) :
      this(
         id = null,
         description = description,
         number = number
      )

   constructor(entity: ShipViaEntity) :
      this(
         id = entity.id,
         description = entity.description,
         number = entity.number
      )

   override fun myId(): UUID? = id
}
