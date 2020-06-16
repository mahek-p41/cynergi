package com.cynergisuite.middleware.region

import com.cynergisuite.domain.Identifiable
import com.cynergisuite.domain.SimpleIdentifiableDTO
import com.cynergisuite.middleware.division.DivisionDTO
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Schema(name = "Region", title = "Region", description = "A region of a division.")
data class RegionDTO(

   @field:Positive
   @field:Schema(name = "id", minimum = "1", required = false, nullable = true, description = "System generated ID")
   var id: Long? = null,

   @field:NotNull
   @field:Schema(name = "name", required = false, nullable = true, description = "Human readable name for a region")
   var name: String? = null,

   @field:NotNull
   @field:Schema(name = "description", required = false, nullable = true, description = "Region description")
   var description: String? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "division", required = false, nullable = true, description = "Division that a region belong to")
   var division: DivisionDTO? = null,

   @field:Valid
   @field:NotNull
   @field:Schema(name = "regionalManager", description = "Regional manager id")
   var regionalManager: SimpleIdentifiableDTO? = null
): Identifiable {
   override fun myId() = id

   constructor(entity: RegionEntity) :
      this(
         id = entity.id,
         name = entity.name,
         description = entity.description,
         regionalManager = SimpleIdentifiableDTO(entity.regionalManager?.id)
      )
}
