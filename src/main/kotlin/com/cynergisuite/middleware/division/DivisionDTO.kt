package com.cynergisuite.middleware.division

import com.cynergisuite.domain.Identifiable
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(name = "Division", title = "Division", description = "A division of a company.")
data class DivisionDTO(

   @field:Schema(name = "id", minimum = "1", description = "System generated ID")
   var id: UUID? = null,

   @field:Positive
   @field:Schema(name = "id", minimum = "1", description = "System Z external number")
   var number: Long? = null,

   @field:NotNull
   @field:Schema(name = "name", description = "Human readable name for a division")
   var name: String? = null,

   @field:NotNull
   @field:Schema(name = "description", description = "Division description")
   var description: String? = null,

) : Identifiable {
   override fun myId(): UUID? = id

   constructor(entity: DivisionEntity) :
      this(
         id = entity.id,
         number = entity.number,
         name = entity.name,
         description = entity.description,
      )
}
